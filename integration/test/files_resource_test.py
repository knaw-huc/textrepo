#!/usr/bin/env python3
import json
import unittest

import requests


class FilesResourceTest(unittest.TestCase):

    def test_post_and_get_file(self):
        es_host = 'http://elasticsearch:9200'
        textrepo_host = 'http://textrepo-app:8080'
        sha = '55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6'
        content = 'hello test'

        self.__clear_files_index(es_host, sha)
        self.__test_post_file(sha, textrepo_host, content)
        self.__test_get_file(sha, textrepo_host, content)
        self.__test_file_is_in_files_index(es_host, sha, content)

    def __clear_files_index(self, es_host, sha):
        clear_index = requests.delete('%s/files' % es_host)
        self.assertEqual(clear_index.status_code, 200)
        test_file_in_index_response = requests.get('%s/files/_doc/%s' % (es_host, sha))
        self.assertEqual(test_file_in_index_response.status_code, 404)

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
