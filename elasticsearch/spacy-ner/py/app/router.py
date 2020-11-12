import sys

import flask
from flask import Response, request, jsonify, send_file
from . import tagger

app = flask.Flask(__name__)
app.config['JSON_SORT_KEYS'] = False
app.url_map.strict_slashes = False

ROOT = '/spacy-ner'


@app.route(ROOT, methods=['GET'])
def home():
    resp = Response(
        response='{"endpoints": ["GET /", "GET /mapping", "POST /fields"]}',
        mimetype="application/json",
        status=200
    )
    return resp


@app.route(ROOT + '/mapping', methods=['GET'])
def mapping():
    return send_file('../../mapping.json', 'application/json')


@app.route(ROOT + '/fields', methods=['POST'])
def tag():
    text = request.form['file']
    short = ((text[:98] + '..') if len(text) > 100 else text).replace('\n', ' ').replace('\r', ' ')
    print('create fields of [' + short + ']', file=sys.stderr)
    entities = tagger.tag(text)
    return jsonify({'contents': text, 'entity': entities})


app.run(host='0.0.0.0', port=8080, debug=False)
