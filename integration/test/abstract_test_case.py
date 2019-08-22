import time
import unittest

import psycopg2
import requests

from integration.test.config import HTTP_ES_HOST, POSTGRES_HOST, POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD


class AbstractTestCase(unittest.TestCase):

    def tearDown(self):
        es_host = HTTP_ES_HOST
        self.__clear_files_index(es_host)
        self.__empty_textrepo_database()

    def __clear_files_index(self, es_host):
        get_index = requests.get('%s/files' % es_host)
        if get_index.status_code == 404:
            return

        clear_index = requests.delete('%s/files' % es_host)
        self.assertEqual(clear_index.status_code, 200)
        get_index = requests.get('%s/files' % es_host)
        self.assertEqual(get_index.status_code, 404)

    def __empty_textrepo_database(self):
        connection = psycopg2.connect(
            host=POSTGRES_HOST,
            database=POSTGRES_DB,
            user=POSTGRES_USER,
            password=POSTGRES_PASSWORD
        )

        with connection.cursor() as cur:
            cur.execute(
                "select truncate_tables_by_username('" + POSTGRES_USER + "');"
            )
            connection.commit()
