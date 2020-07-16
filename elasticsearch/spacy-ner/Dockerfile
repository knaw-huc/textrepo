FROM python:3
WORKDIR /tagger

RUN pip install spacy flask

COPY ./py/ /tagger/py/
COPY ./mapping.json /tagger/mapping.json
COPY ./scripts/wait-for-it.sh /utils/

CMD python /tagger/py/main.py
