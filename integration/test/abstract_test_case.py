import unittest

import requests

from integration.test.config import HTTP_ES_HOST


class AbstractTestCase(unittest.TestCase):

    def tearDown(self):
        es_host = HTTP_ES_HOST
        self.__clear_files_index(es_host)

    def __clear_files_index(self, es_host):
        get_index = requests.get('%s/files' % es_host)
        if get_index.status_code == 404:
            return

        clear_index = requests.delete('%s/files' % es_host)
        self.assertEqual(clear_index.status_code, 200)
        get_index = requests.get('%s/files' % es_host)
        self.assertEqual(get_index.status_code, 404)
