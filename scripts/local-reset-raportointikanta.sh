#!/bin/bash

time curl -N -u 'pää:pää' 127.0.0.1:7021/koski/api/raportointikanta/load?force=true
