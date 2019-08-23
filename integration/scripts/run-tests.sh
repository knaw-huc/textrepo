#!/bin/sh
python3 -m unittest discover -s '/integration/test' -t '/' -p '*_test.py'
