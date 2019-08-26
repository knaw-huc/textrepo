#!/usr/bin/env python3

import json

import requests

from integration.test.abstract_test_case import AbstractTestCase
from integration.test.config import HTTP_ES_HOST, HTTP_APP_HOST


class DocumentsResourceZipTest(AbstractTestCase):

    def test_add_and_get_documents_from_zip(self):
        es_host = HTTP_ES_HOST
        zip_filename = 'integration-test-files.zip'

        locations = self.__test_add_document(zip_filename)

        self.__test_get_latest_version_of_documents(locations)
        self.__test_files_are_in_files_index(es_host, locations)

    def __test_add_document(self, zip_filename):
        zip_file = open("./resources/" + zip_filename, "rb")
        zip_data = zip_file.read()
        zip_file.close()

        multipart_form_data = {
            'file': (zip_filename, zip_data),
        }

        response = requests.post(
            'http://textrepo-app:8080/documents/',
            files=multipart_form_data
        )

        expected_status = 200
        self.assertEqual(response.status_code, expected_status)

        response_json = json.loads(response.text)
        document_locations = response_json['locations']
        self.assertEqual(len(document_locations), 2)

        return document_locations

    def __test_get_latest_version_of_documents(self, locations):
        sha_1 = 'bf83caeb324ec7d185e8bf266d97ca9354732b76eee46b6227adb082'

        sha_2 = '4fcc5672305c87f66f09c39f7229fd126cab95edf30821ec9ee4a66f'

        self.__test_get_latest_version(locations['een.txt'], "Een.\n", sha_1)
        self.__test_get_latest_version(locations['twee.txt'], "Twee.\n", sha_2)

    def __test_get_latest_version(self, document_location, content, sha):
        status = 200

        get_version_response = requests.get(HTTP_APP_HOST + document_location)
        self.assertEqual(get_version_response.status_code, status)
        response_json = json.loads(get_version_response.text)
        self.assertEqual(response_json['fileSha'], sha)

        get_file_response = requests.get('http://textrepo-app:8080/files/%s'
                                         % response_json['fileSha'])
        self.assertEqual(get_file_response.status_code, status)
        self.assertEqual(get_file_response.text, content)

    def __test_files_are_in_files_index(self, es_host, locations):
        self.__test_file_in_index("Een.\n", locations['een.txt'], es_host)
        self.__test_file_in_index("Twee.\n", locations['twee.txt'], es_host)

    def __test_file_in_index(self, content, location, es_host):
        doc_id = location.rsplit('/', 1)[-1]
        response = requests.get('%s/documents/_doc/%s' % (es_host, doc_id))
        self.assertEqual(response.status_code, 200)
        response_json = json.loads(response.text)
        self.assertEqual(response_json['_source']['content'], content)
