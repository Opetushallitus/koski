# Koski-loggaus

Koski-tietojen muutoksista ja katselusta logataan tapahtuma Kosken audit-logiin. Lisäksi Koski tallennetaan
erillisiin logeihin mm. performanssi- ja diagnostiikkadataa. Kaikki logit kerätään keskitettyyn Elasticsearch
-tietokantaan, josta logitapahtumia voi seurata Kibana-käyttöliittymän avulla.

Loggaus tapahtuu slf4j-loggauskehyksellä, joka on konfiguroitu käyttämään log4j:tä. Kaikki loggaus tehdään paikallisiin
tiedostoihin, joista ne kopioidaan Filebeat-agentin ja Logstashin avulla keskitettyyn Elasticsearch-logikantaan. Filebeat
on konfiguroitu niin, että keskeiset logattavat kentät parsitaan logitiedostoista omiin kenttiinsä Elasticsearcissa, jolloin
logien katselu ja tietojen haku on mahdollisimman helppoa.

## Audit-loggaus

Kaikki tärkeät tapahtumat kirjataan Audit-logiin, jonka avulla voidaan selvittää, kuka on katsellut tai muuttanut
oppijan tietoja. Logattaviin tapahtumiin sisältyy

- Opiskeluoikeuden lisäys
- Opiskeluoikeuden muutos (muutokset opiskeluoikeuden tai sen alle tallennettujen suoritusten tietoihin, ml. suorituksen lisäys)
- Opiskeluoikeuden katselu (tietojen katselu Koski-käyttöliittymästä tai sovellusrajapinnan avulla)
- Henkilön tietojen haku (esimerkiksi henkilön haku nimellä tai henkilötunnuksella)
- Opiskeluoikeuksien haku (useamman opiskeluoikeuden tietojen haku rajapinnan avulla)
- Automaattinen tiedonsiirto ulkoisesta järjestelmästä Koskeen

Tarkka lista logattavista tapahtumista [sovelluskoodissa](https://github.com/Opetushallitus/koski/blob/600c51031dc1906851bae79a587386f38723e1f1/src/main/scala/fi/oph/koski/log/AuditLog.scala#L41).

Kaikista tapahtumista kirjataan

- Aikaleima
- Käyttäjän OID
- Käyttäjän nimi
- Käyttäjän IP-osoite
- Operaation tunniste (esimerkiksi OPISKELUOIKEUS_LISAYS)

Lisäksi kerätään seuraavat tiedot aina, kun mahdollista

- Oppijan OID (kun operaatio kohdistuu yhteen oppijaan)
- Opiskeluoikeuden ID (kun operaatio kohdistuu yhteen opiskeluoikeuteen)
- Opiskeluoikeuden versio
- Hakuehto

Audit-logi tuotetaan kullakin sovelluspalvelininstanssilla paikalliseen tiedostoon `koski-audit.log`.

Esimerkkirivi:

    {"timestamp":"2017-02-14 10:35:31.005","serviceName":"koski","applicationType":"backend","opiskeluoikeusId":"1957","oppijaHenkiloOid":"1.2.246.562.24.00000000011","clientIp":"0:0:0:0:0:0:0:1","kayttajaHenkiloOid":"1.2.246.562.24.99999999987","operaatio":"OPISKELUOIKEUS_MUUTOS","opiskeluoikeusVersio":"2"}
    
Audit-logitiedosto tuotetaan JSON-muodossa, hyödyntäen Opintopolun yhteistä [Auditlogger](https://github.com/Opetushallitus/auditlogger)-komponenttia.

## Sovelluslogi `koski.log` 

Sovelluslogi, johon tuotetaan audit-logia yksityiskohtaisempaa tietoa tapahtumista, sekä mahdolliset sovellusvirheet
ja varoitukset.

Esimerkkirivi:

    2017-02-14 10:35:31 INFO  KoskiOppijaFacade - kalle(1.2.246.562.24.99999999987)@0:0:0:0:0:0:0:1 Päivitetty opiskeluoikeus 1957 (versio 2) oppijalle 1.2.246.562.24.00000000011 tutkintoon koulutus/361902 oppilaitoksessa 1.2.246.562.10.52251087186

### Access log `koski-access.log` 

Jettyn generoima sisään tulevien HTTP-pyyntöjen logi.

Esimerkkirivi:

    0:0:0:0:0:0:0:1 - - [14/Feb/2017:07:37:11 +0000] "GET /koski/api/opiskeluoikeus/1957 HTTP/1.1" 200 3102  34

### HTTP-pyyntöjen logi `koski-httpclient.log`

Kosken ulospäin tekemien HTTP-pyyntöjen logi.

Esimerkkirivi:

    2017-02-10 10:41:01 GET https://extra.koski.opintopolku.fi/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&oid=1.2.246.562.10.23152948685 status 200 took 384 ms
    
Samat metriikkatiedot lähetetään myös Prometheus-monitorintijärjestelmään, josta ne visualisoidaan Grafanalla.    

### Performanssilogi `koski-performance.log` 

Valittujen ajastettujen koodiblokkien ajastuslogi, jota hyödynnetään sovelluksen suorituskyvyn tarkkailussa ja
optimoinnissa. 

Esimerkkirivi:

    2017-02-01 09:24:09 INFO  PostgresOpiskeluoikeusRepository - createOrUpdate took 135 ms
    
Samat metriikkatiedot lähetetään myös Prometheus-monitorintijärjestelmään, josta ne visualisoidaan Grafanalla.