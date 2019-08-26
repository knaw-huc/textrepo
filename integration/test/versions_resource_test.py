#!/usr/bin/env python3
import json

import requests

from integration.test.abstract_test_case import AbstractTestCase
from integration.test.config import HTTP_APP_HOST


class VersionsResourceTest(AbstractTestCase):

    def test_post_files_and_get_versions(self):
        textrepo_host = HTTP_APP_HOST
        sha1 = '55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6'
        content1 = 'hello test'

        sha2 = 'ef2c433609e63cb570b1392d3461bf53da03c54bdde0ab33aa806d59'
        content2 = 'hello test 2'

        location = self.__test_post_new_document(content1, textrepo_host)
        document_id = location.rsplit('/', 1)[-1]

        self.__test_put_updated_document(content2, document_id, textrepo_host)
        self.__test_get_versions(document_id, sha1, sha2, textrepo_host)

    def __test_post_new_document(self, content, host):
        multipart_form_data = {
            'file': ('file.txt', content),
        }

        response = requests.post(
            '%s/documents' % host,
            files=multipart_form_data
        )

        expected_status = 201
        self.assertEqual(response.status_code, expected_status)

        location = response.headers['Location']
        self.assertIsNotNone(location)

        return location

    def __test_put_updated_document(self, content, id, host):
        multipart_form_data = {
            'file': ('file.txt', content),
        }

        response = requests.put(
            '%s/documents/%s/files' % (host, id),
            files=multipart_form_data
        )

        expected_status = 200
        self.assertEqual(response.status_code, expected_status)

    def __test_get_versions(self, document_id, sha1, sha2, host):
        response = requests.get('%s/documents/%s/versions' % (host, document_id))

        expected_status = 200
        self.assertEqual(response.status_code, expected_status)

        response_json = json.loads(response.text)
        self.assertEqual(response_json[0]['documentUuid'], document_id)
        self.assertEqual(response_json[0]['fileSha'], sha1)
        self.assertEqual(response_json[1]['documentUuid'], document_id)
        self.assertEqual(response_json[1]['fileSha'], sha2)
