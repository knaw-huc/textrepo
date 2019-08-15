#!/usr/bin/env python3

import unittest
import requests
import json


class FilesResourceTest(unittest.TestCase):

    def test_add_get_replace_document(self):

        file_content='hello test'
        file_sha='55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6'
        document_location = self.__test_add_document(file_content)

        self.__test_get_latest_version_of_document(document_location, file_content, file_sha)

        updated_file_content='hello test 2'
        updated_file_sha='ef2c433609e63cb570b1392d3461bf53da03c54bdde0ab33aa806d59'
        self.__test_replace_document(document_location, updated_file_content, updated_file_sha)

        self.__test_get_latest_version_of_document(document_location, file_content, file_sha)

    def __test_add_document(self, content):
        multipart_form_data = {
            'file': ('file.txt', content),
        }

        response = requests.post(
            'http://textrepo-app:8080/documents/',
            files=multipart_form_data
        )

        expected_status = 201
        self.assertEqual(response.status_code, expected_status)

        document_location = response.headers['Location']
        self.assertIsNotNone(document_location)
        return document_location

    def __test_get_latest_version_of_document(self, document_location, expected_content, expected_sha):
        get_version_response = requests.get(document_location)

        expected_status = 200
        self.assertEqual(get_version_response.status_code, expected_status)

        response_json = json.loads(get_version_response.text)
        self.assertEqual(response_json['fileSha'], expected_sha)

        get_file_response = requests.get('http://textrepo-app:8080/files/%s' % response_json['fileSha'])
        expected_status = 200
        self.assertEqual(get_file_response.status_code, expected_status)

        self.assertEqual(get_file_response.text, expected_content)

    def __test_replace_document(self, document_location, updated_file_content, updated_file_sha):
        print('TODO: create __test_replace_document')

