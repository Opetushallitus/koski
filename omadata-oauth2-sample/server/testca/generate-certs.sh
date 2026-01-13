#!/bin/bash

set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR

if [ -d certs -a "$1" != "-f" ]; then
  exit 0
fi

rm -rf certs csr db private newcerts
mkdir -p certs csr db private newcerts
touch db/root-ca.index
echo 1000 > db/root-ca.serial
echo 1000 > db/root-ca.crlnumber

openssl genrsa -out private/root-ca.key 2048
openssl req -config openssl.cnf -key private/root-ca.key \
    -new -x509 -days 3650 -sha256 -extensions root_ca \
    -subj '/CN=TestCA' \
    -out certs/root-ca.crt

openssl genrsa -out private/proxy.key 2048
openssl req -config openssl.cnf -key private/proxy.key -new -sha256 -out csr/proxy.req -subj '/CN=localhost'
openssl ca -config openssl.cnf -in csr/proxy.req -out certs/proxy.crt -extensions server_cert -notext -batch

openssl genrsa -out private/client.key 2048
openssl req -config openssl.cnf -key private/client.key -new -sha256 -out csr/client.req -subj '/C=FI/O=Testi/CN=client.example.com'
openssl ca -config openssl.cnf -in csr/client.req -out certs/client.crt -extensions server_cert -notext -batch
