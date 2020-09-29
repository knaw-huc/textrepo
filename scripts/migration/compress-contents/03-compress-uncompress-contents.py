#!/usr/bin/env python3

import gzip
import json
import os
import psycopg2         # may require pip/pip3 install?
import psycopg2.extras  # for DictCursor
import requests

textrepo = 'http://localhost:8080/textrepo'

todo_query = """
    SELECT sha224 FROM contents
    WHERE compressed IS NULL
      AND SUBSTRING(contents FROM 1 FOR 2) != '\\x1f8b'
    """

get_query = "SELECT contents FROM contents WHERE sha224 = %s"

put_query = "UPDATE contents SET compressed = %s WHERE sha224 = %s"

try:
    with psycopg2.connect(host='localhost', port=5432, user='textrepo', password='textrepo') as conn:
        #print ( conn.get_dsn_parameters(),"\n") # connection debug output

        # DictCursor exposes results as dictionary, allowing us to do row['sha224'] instead of row[0]
        with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cursor:
            cursor.execute(todo_query)
            rows = cursor.fetchall()
            for row in rows:
                sha224 = row['sha224']
                print("Compressing contents for sha224:", sha224)
                cursor.execute(get_query, (sha224,))
                record = cursor.fetchone()
                uncompressed = record['contents']
                compressed = gzip.compress(uncompressed)
                cursor.execute(put_query, (compressed, sha224))
                conn.commit()
                print("\tuncompressed len =", len(uncompressed), "-> compressed len =", len(compressed))

except (Exception, psycopg2.Error) as error:
    print("Error while getting data from postgres: ", error)

finally:
    if (conn):
        cursor.close()
        conn.close()
        print("postgres connection closed")
