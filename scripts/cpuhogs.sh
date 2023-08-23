#!/bin/bash
# Aiheuttaa virheitä GA-buildereillä, siksi kommentoitu pois
#set -euo pipefail

echo
echo
echo "========================================================================="
echo "CPU hogs:"
ps -Ao pid,pcpu,rss,cmd --sort=-rss | head -n 10
echo "========================================================================="
