#!/usr/bin/env python3

import json

import requests

from integration.test.abstract_test_case import AbstractTestCase
from integration.test.config import HTTP_ES_HOST, HTTP_APP_HOST


class DocumentsResourceTest(AbstractTestCase):

    def test_add_get_document(self):
        es_host = HTTP_ES_HOST
        textrepo_host = HTTP_APP_HOST

        file_content = 'hello test'
        file_sha = '55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6'
        location = self.__test_add_document(file_content, textrepo_host)
        document_id = location.rsplit('/', 1)[-1]

        self.__test_get_latest_version(location, file_content, file_sha, textrepo_host)
        self.__test_file_is_in_files_index(es_host, document_id, file_content)
        self.__test_get_latest_version(location, file_content, file_sha, textrepo_host)

    def __test_add_document(self, content, host):
        multipart_form_data = {
            'file': ('file.txt', content),
        }

        response = requests.post(
            '%s/documents/' % host,
            files=multipart_form_data
        )

        expected_status = 201
        self.assertEqual(response.status_code, expected_status)

        document_location = response.headers['Location']
        self.assertIsNotNone(document_location)

        return document_location

    def __test_get_latest_version(self, location, content, sha, host):
        get_version_response = requests.get(location)

        expected_status = 200
        self.assertEqual(get_version_response.status_code, expected_status)

        response_json = json.loads(get_version_response.text)
        self.assertEqual(response_json['fileSha'], sha)

        get_file_response = requests.get(
            '%s/files/%s' % (host, response_json['fileSha'])
        )
        expected_status = 200
        self.assertEqual(get_file_response.status_code, expected_status)

        self.assertEqual(get_file_response.text, content)

    def __test_file_is_in_files_index(self, es_host, document_id, content):
        test_file_in_index_response = requests.get(
            '%s/documents/_doc/%s' % (es_host, document_id)
        )
        self.assertEqual(test_file_in_index_response.status_code, 200)
        response_json = json.loads(test_file_in_index_response.text)
        self.assertEqual(response_json['_source']['content'], content)