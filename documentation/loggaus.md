# Koski-loggaus

Koski-tietojen muutoksista ja katselusta logataan tapahtuma Kosken audit-lokiin. Lisäksi Koski tallennetaan
erillisiin lokeihin mm. performanssi- ja diagnostiikkadataa.

Loggaus tapahtuu slf4j-loggauskehyksellä, joka on konfiguroitu käyttämään log4j2:tä.

Tuotanto- ja testiympäristöissä docker-kontainerin rinnalla ajetaan Fluent Bitiä, joka parsii, filtteröi ja ohjaa Kosken stdoutiin
tulostamat logirivit FireLensin kautta AWS CloudWatchiin oikeisiin lokiryhmiin.

Paikallisesti kehittäessä logit kirjoitetaan `/logs`-kansiossa oleviin tiedostoihin. Tiedostoja ei kirjoiteta tuotanto- eikä
testiympäristöissä. Tiedostot olivat aikanaan tapa lokien välittämiseen Koskesta eteenpäin, mutta nykyään ne ovat käytössä
vain kehitystyön helpottamiseksi diagnoosi- ja debug-apuna.

<img src="./loggaus.svg" />

## Konfiguraatiot

- Tuotanto- ja testiympäristöt: `/docker-build/log4j2.xml`
- Paikallinen kehitys: `/src/main/resources/log4j2-local.xml`
- Testit: `/src/main/resources/log4j2-test.xml`

## Audit-loggaus `oma-opintopolku-audit-loki`

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

Audit-logit siirtyvät Cloudwatchin lokiryhmään `oma-opintopolku-audit-loki`. Tämän lisäksi ne siirretään
[oma-opintopolku-loki](https://github.com/Opetushallitus/oma-opintopolku-loki) palveluun, joka tuottaaa kansalaiselle
"kuka katsoi tietojani" näkymän.

Esimerkkirivi:

    {"timestamp":"2017-02-14 10:35:31.005","serviceName":"koski","applicationType":"backend","opiskeluoikeusId":"1957","oppijaHenkiloOid":"1.2.246.562.24.00000000011","clientIp":"0:0:0:0:0:0:0:1","kayttajaHenkiloOid":"1.2.246.562.24.99999999987","operaatio":"OPISKELUOIKEUS_MUUTOS","opiskeluoikeusVersio":"2"}

Audit-logitiedosto tuotetaan JSON-muodossa, hyödyntäen Opintopolun yhteistä [Auditlogger](https://github.com/Opetushallitus/auditlogger)-komponenttia.

## Access log `koski-access.log`

Jettyn generoima sisään tulevien HTTP-pyyntöjen logi.

Esimerkkirivi:

    0:0:0:0:0:0:0:1 - - [14/Feb/2017:07:37:11 +0000] "GET /koski/api/opiskeluoikeus/1957 HTTP/1.1" 200 3102  34

## HTTP-pyyntöjen logi `koski-httpclient.log`

Kosken ulospäin tekemien HTTP-pyyntöjen logi.

Esimerkkirivi:

    2017-02-10 10:41:01 GET https://extra.koski.opintopolku.fi/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&oid=1.2.246.562.10.23152948685 status 200 took 384 ms

Samat metriikkatiedot lähetetään myös Prometheus-monitorintijärjestelmään, josta ne visualisoidaan Grafanalla.

## Performanssilogi `koski-performance.log`

Valittujen ajastettujen koodiblokkien ajastuslogi, jota hyödynnetään sovelluksen suorituskyvyn tarkkailussa ja
optimoinnissa.

Esimerkkirivi:

    2017-02-01 09:24:09 INFO  PostgresOpiskeluoikeusRepository - createOrUpdate took 135 ms

Samat metriikkatiedot lähetetään myös Prometheus-monitorintijärjestelmään, josta ne visualisoidaan Grafanalla.

## IP-seurantalogi `koski-ip-tracking.log`

Logi, johon tallennetaan tiedonsiirtoon oikeutettujen käyttäjien IP-osoitteiden muutokset.

Esimerkkirivi:

    2017-07-25 12:21:30 oppilaitos-integraattori(1.2.246.562.10.50363000001), vanha: 10.222.22.1, uusi: 10.222.23.1

## Sovelluslogi `koski.log`

Logi, johon tuotetaan yksityiskohtaista tietoa muista tapahtumista, sekä mahdolliset sovellusvirheet ja varoitukset.

Esimerkkirivi:

    2017-02-14 10:35:31 INFO  KoskiOppijaFacade - kalle(1.2.246.562.24.99999999987)@0:0:0:0:0:0:0:1 Päivitetty opiskeluoikeus 1957 (versio 2) oppijalle 1.2.246.562.24.00000000011 tutkintoon koulutus/361902 oppilaitoksessa 1.2.246.562.10.52251087186
