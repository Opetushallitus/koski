#!/bin/bash
set -eou pipefail
echo "Running mocha tests against $1"
(cd $(dirname "$0")/../web && node_modules/mocha-phantomjs/bin/mocha-phantomjs -p node_modules/phantomjs/bin/phantomjs $1)