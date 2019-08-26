#!/usr/bin/env python3

import requests

from integration.test.abstract_test_case import AbstractTestCase
from integration.test.config import HTTP_APP_HOST


class FilesResourceTest(AbstractTestCase):

    def test_post_and_get_file(self):
        textrepo_host = HTTP_APP_HOST
        sha = '55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6'
        content = 'hello test'

        self.__test_post_file(sha, content, textrepo_host)
        self.__test_get_file(sha, content, textrepo_host)

    def __test_post_file(self, sha, content, textrepohost):
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

    def __test_get_file(self, sha, content, textrepo_host):
        response = requests.get((textrepo_host + '/files/%s') % sha)

        expected_status = 200
        self.assertEqual(response.status_code, expected_status)

        expected_text = content
        self.assertEqual(response.text, expected_text)
