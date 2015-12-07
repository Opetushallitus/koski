#!/bin/bash -e -o pipefail


(cd $(dirname "$0")/../web && node_modules/mocha-phantomjs/bin/mocha-phantomjs $1 -R xunit | grep -E '</?test' > target/test-report.xml)

