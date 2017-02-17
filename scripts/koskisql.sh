#!/bin/bash


if [ -z $1 ]; then
  echo koskisql establishes an ssh tunnel to given host and runs psql over that connection.
  echo "USAGE: koskisql server [arg1 [arg2 ...]]"
  exit 1
fi

function cleanup {
  ps -ef|grep "$COMMAND"|grep -v grep|awk '{print $2}'|xargs kill
}

trap "cleanup" EXIT
SERVER=$1
COMMAND="ssh -f -N -L 5433:localhost:5432 $SERVER"
$COMMAND
psql -h localhost -p 5433 "${@:2}"
