#!/bin/bash
set -eou pipefail
echo "Running mocha tests against $1"
(cd $(dirname "$0")/../web && ./node_modules/.bin/phantomjs ./node_modules/mocha-phantomjs-core/mocha-phantomjs-core.js $1)
