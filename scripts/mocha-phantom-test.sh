#!/bin/bash
set -eou pipefail

(cd $(dirname "$0")/../web && node_modules/mocha-phantomjs/bin/mocha-phantomjs $1 -R xunit | grep -E '</?test' > test-report.xml)

