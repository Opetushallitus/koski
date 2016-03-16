#!/bin/bash
set -eou pipefail
mkdir web/target 2>/dev/null||true
(cd $(dirname "$0")/../web && node_modules/mocha-phantomjs/bin/mocha-phantomjs $1 -R xunit | grep -E '</?test' > ./target/TEST-mocha.xml)

