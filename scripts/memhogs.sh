#!/bin/bash
# Aiheuttaa virheitä GA-buildereillä, siksi kommentoitu pois
#set -euo pipefail

echo
echo
echo "========================================================================="
echo "MEM hogs:"
ps -Ao pid,pcpu,rss,cmd --sort=-pcpu | head -n 10
echo "========================================================================="
