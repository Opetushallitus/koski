#!/bin/bash
#set -euo pipefail

echo "Filehogs:"
for x in `ps -eF| awk '{ print $2 }'`;do echo `ls /proc/$x/fd 2> /dev/null | wc -l` $x `cat /proc/$x/cmdline 2> /dev/null`;done 2> /dev/null | sort -n -r | head -n 10
