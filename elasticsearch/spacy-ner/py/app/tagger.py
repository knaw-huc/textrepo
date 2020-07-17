import spacy

nlp = spacy.load('/tagger/model/')

def tag(text):
    result = []
    for e in nlp(text).ents:
        result.append({'text': e.text, 'label': e.label_, 'start': e.start_char, 'end': e.end_char})
    return result
