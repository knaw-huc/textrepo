#!/usr/bin/env python3

import unittest

import requests


class PostFileTest(unittest.TestCase):

    def testPostFile(self):
        multipart_form_data = {
            'file': ('file.txt', 'hello test'),
        }

        response = requests.post(
            'http://textrepo-app:8080/files/',
            files=multipart_form_data
        )

        expectedStatus = 200
        self.assertEqual(response.status_code, expectedStatus)

        expectedText = '{"sha":"55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6"}'
        self.assertEqual(response.text, expectedText)
