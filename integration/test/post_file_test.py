#!/usr/bin/env python3

import unittest

import requests


class FileResourceTest(unittest.TestCase):

    def test_post_and_get_file(self):
        self.__test_post_file()
        self.__test_get_file()

    def __test_post_file(self):
        multipart_form_data = {
            'file': ('file.txt', 'hello test'),
        }

        response = requests.post(
            'http://textrepo-app:8080/files/',
            files=multipart_form_data
        )

        expected_status = 201
        self.assertEqual(response.status_code, expected_status)

        expected_text = '{"sha224":"55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6"}'
        self.assertEqual(response.text, expected_text)

    def __test_get_file(self):
        sha = '55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6'
        response = requests.get('http://textrepo-app:8080/files/%s' % sha)

        expected_status = 200
        self.assertEqual(response.status_code, expected_status)

        expected_text = "hello test"
        self.assertEqual(response.text, expected_text)
