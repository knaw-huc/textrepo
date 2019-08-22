import os

HTTP_ES_HOST = 'http://' + os.environ['ES_HOST']
HTTP_APP_HOST = 'http://' + os.environ['APP_HOST']

POSTGRES_HOST = os.environ['POSTGRES_HOST']
POSTGRES_PASSWORD = os.environ['POSTGRES_PASSWORD']
POSTGRES_DB = os.environ['POSTGRES_DB']
POSTGRES_USER = os.environ['POSTGRES_USER']
