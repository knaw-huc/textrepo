FROM python:3.6-alpine

WORKDIR /integration/test
COPY ./scripts/* /utils/
RUN pip install requests
RUN apk update && apk add postgresql-dev gcc python3-dev musl-dev
RUN pip install psycopg2

RUN apk add --no-cache bash
