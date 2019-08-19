#!/usr/bin/env python3
import json
import unittest

import requests

from integration.test.abstract_test_case import AbstractTestCase
from integration.test.config import HTTP_ES_HOST, HTTP_APP_HOST


class FilesResourceTest(AbstractTestCase):

    def test_post_and_get_file(self):
        es_host = HTTP_ES_HOST
        textrepo_host = HTTP_APP_HOST
        sha = '55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6'
        content = 'hello test'

        self.__test_post_file(sha, textrepo_host, content)
        self.__test_get_file(sha, textrepo_host, content)
        self.__test_file_is_in_files_index(es_host, sha, content)

    def __test_post_file(self, sha, textrepohost, content):
        multipart_form_data = {
            'file': ('file.txt', content),
        }

        response = requests.post(
            '%s/files/' % textrepohost,
            files=multipart_form_data
        )

        expected_status = 201
        self.assertEqual(response.status_code, expected_status)

        expected_text = '{"sha224":"%s"}' % sha
        self.assertEqual(response.text, expected_text)

    def __test_get_file(self, sha, textrepo_host, content):
        response = requests.get((textrepo_host + '/files/%s') % sha)

        expected_status = 200
        self.assertEqual(response.status_code, expected_status)

        expected_text = content
        self.assertEqual(response.text, expected_text)

    def __test_file_is_in_files_index(self, es_host, sha, content):
        test_file_in_index_response = requests.get('%s/files/_doc/%s' % (es_host, sha))
        self.assertEqual(test_file_in_index_response.status_code, 200)
        response_json = json.loads(test_file_in_index_response.text)
        self.assertEqual(response_json['_source']['content'], content)
