FROM python:3.8

RUN pip install spacy flask

WORKDIR /indexer
COPY ./py/ /indexer/py/
COPY ./mapping.json /indexer/mapping.json
COPY ./scripts/start.sh /indexer/

CMD ["./start.sh"]
