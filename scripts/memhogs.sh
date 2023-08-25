#!/bin/bash
# Aiheuttaa virheitä GA-buildereillä, siksi kommentoitu pois
#set -euo pipefail

echo
echo
echo "============================"
echo "MEM hogs:"
date
ps -Ao pid,pcpu,rss,cmd --sort=-rss | head -n 10
date
echo "============================"
echo
