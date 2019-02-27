#!/bin/bash

time curl -N -u 'pää:pää' 127.0.0.1:7021/koski/api/raportointikanta/clear
time curl -N -u 'pää:pää' 127.0.0.1:7021/koski/api/raportointikanta/opiskeluoikeudet
time curl -N -u 'pää:pää' 127.0.0.1:7021/koski/api/raportointikanta/henkilot
time curl -N -u 'pää:pää' 127.0.0.1:7021/koski/api/raportointikanta/organisaatiot
time curl -N -u 'pää:pää' 127.0.0.1:7021/koski/api/raportointikanta/koodistot
