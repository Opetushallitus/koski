# Koski

Koski tulee toimimaan kattavana opetustoimialan tietovarantona, joka tarjoaa
tutkintoon johtavat suoritustiedot eri koulutusasteilta. Yleinen Koski-dokumentaatio kootaan CSC:n wikiin: https://confluence.csc.fi/display/OPHPALV/Koski

Tässä git-repositoriossa on Koski-järjestelmän ohjelmakoodi, tietokannan rakennuslausekkeet ja tekninen dokumentaatio ohjelmistokehitystä varten.

Koski on [EUPL](LICENSE.txt)-lisensoitu sovellus, josta on mahdollista käynnistää kehitysinstanssi omalla työasemalla, alla olevien kehitysohjeiden mukaisesti. Koski-sovellus on alustariippumaton, sillä se pyörii Java-virtuaalikoneella. Kehitysympäristö toimii sellaisenaan ainakin Linux ja OSX-käyttöjärjestelmissä.

Huom! Koski-järjestelmän kehitys on vielä alkuvaiheessa ja kaikki tässä dokumentissa kuvattu voi vielä muuttua matkan varrella.

## Käsitteet

Keskeiset entiteetit, ja järjestelmät, joihin nämä tallennetaan.

| käsite         | selite                                       | tunniste         | tallennuspaikka        |
|----------------|----------------------------------------------|------------------|------------------------|
| Koodisto       | Kooditus objekteille, esim tutkintonimikkeet | id (tekstiä)     | Koodistopalvelu        |
| Koodi          | Yksittäisen objektin koodi koodistossa       | id (tekstiä)     | Koodistopalvelu        |
| Oppija         | Opiskelija, oppilas.                         | henkilöOid       | Henkilöpalvelu         |
| Organisaatio   | Oppilaitos, kunta, eri rooleissa             | organisaatioOid  | Organisaatiopalvelu    |
| Opinto-oikeus  | Oppijan suhde oppilaitokseen ja suoritettavaan tutkintoon (tutkinto, oppija, oppilaitos, voimassaoloaika, läsnäolotiedot...) | id (numeerinen)  | Koski        |
| Peruste        | Tutkinnon tai tutkinnon osan peruste         | diaarinumero     | ePerusteet             |
| Suoritus       | Oppijan suoritus (tutkinto, oppija, oppilaitos, aika...) | id (numeerinen)  | Koski        |
| Tutkinto       | Tutkinnon kuvaus (tutkintotunnus, nimi...)   | tutkintotunnus   | Koodisto               |

## Teknologiat

Nämä ovat keskeiset Koski-järjestelmässä käytettävät teknologiat. Lista kuvaa järjestelmän nykytilaa ja muuttuu matkan varrella
tarpeen mukaan.

- PostgreSQL 9.6 -tietokanta
- Palvelinteknologiat
  - Scala 2.11.4 ohjelmointikieli ja kääntäjä
  - Scalatra web framework
  - Slick (http://slick.typesafe.com/doc/3.0.1/index.html) relaatiokantakirjasto
  - Flyway migraatiotyökalu kannan skeeman rakentamiseen ja päivittämiseen kehityksessä ja tuotannossa
  - Maven build-työkalu kehityskäyttöön ja asennettavan paketin rakentamiseen
  - Mvn-riippuvuuksien lataus Jitpackilla, jolloin voidaan viitata suoraan Github-repoihin, eikä tarvitse itse buildata jar-artifaktoja
  - Integraatiot Opintopolku-palveluihin, kuten organisaatio- ja henkilöpalveluun REST-rajpinnoilla, käyttäen http4s-clienttiä
- Web-sovelluksen frontend-teknologiat
  - NPM riippuvuuksien hakuun   
  - Webpack frontend bundlen rakentamiseen
  - React
  - Bacon.js
  - LESS
  - Selainyhteensopivuus IE10-selaimesta moderneihin selaimiin
- Koko järjestelmän buildaus Make-työkalulla, joka delegoi yksittäiset toiminnot eri työkaluille, kuten Maven ja NPM

## Kehitystyökalut

Minimissään tarvitset nämä:

- Git (osx, linux sisältää tämän, komentorivillä `git`)
- GNU Make (osx, linux sisältää tämän, komentorivillä `make`)
- Java 8
- Maven 3 (osx: `brew install maven`)
- Postgres 9.6 (osx: `brew install postgresql`)
- Node.js ja NPM (osx: `brew install node`)
- Tekstieditori (kehitystiimi käyttää IntelliJ IDEA 14/15)

## Buildi ja ajaminen

Koski:n buildiin kuuluu frontin buildaus (npm / webpack) ja serverin buildaus Mavenilla. Tätä helpottamaan on otettu käyttöön `make`, jonka avulla
eri taskit on helppo suorittaa. Ks `Makefile`-tiedosto.

Buildaa koko systeemi

    make build    

Buildaa frontti, ja päivitä automaattisesti kun tiedostoja muokataan:

    make watch
    
Staattinen analyysi [ScalaStyle](http://www.scalastyle.org/) ja [ESLint](http://eslint.org/)

    make lint

### Koski-sovelluksen ajaminen kehitystyöasemalla

Aja JettyLauncher-luokka IDEAsta/Eclipsestä, tai käynnistä Koski vaihtoehtoisesti komentoriviltä

    make build
    make run

Avaa selaimessa

    http://localhost:7021/koski

Suoritus-testidatat näkyy

    http://localhost:7021/koski/suoritus/

### Ajaminen paikallisesti käyttäen ulkoisia palveluja (esim henkilöpalvelu)

Ilman parametrejä ajettaessa Koski käyttää mockattuja ulkoisia riippuvuuksia.

Ottaaksesi käyttöön ulkoiset integraatiot, kuten henkilpalvelun, voit antaa Koski:lle käynnistysparametrinä käytettävän konfiguraatiotiedoston sijainnin. Esimerkiksi

    -Dconfig.resource=qa.conf

Tällä asetuksella käytetään tiedostoa `src/main/resources/qa.conf`. Tämä tiedosto ei ole versionhallinnassa, koska se sisältää ei-julkista tietoa.

## Paikallinen PostgreSQL-tietokanta

Kehityskäyttöön tarvitaan paikallinen PostgreSQL-tietokanta. Koski-sovellus luo paikallisen kannan, skeeman ja käyttäjän
automaattisesti ja käynnistää myös tarvittavan PostgreSQL-serveriprosessin.

Paikallisen kannan konfiguraatio on tiedostossa `postgresql/postgresql.conf` ja tietokannan datahakemisto on `postgresql/data`.

Jos haluat pitää Postgresin käynnissä erikseen, voit käynnistää sen komentoriviltä komennolla

    make postgres

PostgreSQL jää pyörimään konsoliin ja voit sammuttaa sen painamalla ctrl-c.

Käynnistyessään Koski-sovellus huomaa, jos tietokanta on jo käynnissä, eikä siinä tapauksessa yritä käynnistää sitä.

Kehityksessä käytetään kahta kantaa: `koski` jota käytetään normaalisti ja `koskitest` jota käytetään automaattisissa
testeissä (tämä kanta tyhjennetään aina testiajon alussa). Molemmat kannat sisältävät `koski` -skeeman, ja sijaitsevat
fyysisesti samassa datahakemistossa.


### SQL-yhteys paikalliseen kantaan

Jos ja kun haluat tarkastella paikallisen kehityskannan tilaa SQL-työkalulla, se onnistuu esimerkiksi Postgren omalla komentorivityökalulla `psql`:

    psql -h localhost koski koski
    psql -h localhost koskitest koski

Peruskomennot

    \dt    listaa taulut
    \q     poistuu psql:stä

Sitten vaikka

    select * from arviointi;

### Kantamigraatiot

Tietokannan rakenne luodaan ja päivitetään Flywayllä migraatioskripteillä, jotka ovat hakemistossa `src/main/resources/db/migration`.

Koski-sovellus ajaa migraatiot automaattisesti käynnistyessään.

### Testit

Buildaa ja aja kaikki testit

    make test

Kun applikaatio pyörii paikallisesti (ks. ohjeet yllä), voi Mocha-testit ajaa selaimessa osoitteessa

    http://localhost:7021/koski/test/runner.html

Mocha-testit voi ajaa myös nopeasti komentoriviltä

    make fronttest

## CI-palvelin

Koski:n Jenkins CI-palvelin palvelee osoitteessa https://dev.koski.opintopolku.fi/jenkins/, jonne pääsy on rajattu käyttäjätunnuksella ja salasanalla.

CI-palvelimella sovellus testataan jokaisen commitin yhteydessä. Paikallisten testien lisäksi ajetaan pieni määrä integraatiotestejä testiympäristön REST-rajapintoja vasten.
 
Myös staattinen analyysi [ScalaStyle](http://www.scalastyle.org/) ja [ESLint](http://eslint.org/) -työkaluilla ajetaan joka commitille.

Suorituskykytestit ajetaan joka aamu. 

CI-palvelimen konfiguraatio synkronoidaan [Github-repositorioon](https://github.com/Opetushallitus/koski-ci-configuration) Jenkins SCM sync congiguration [pluginilla](https://wiki.jenkins-ci.org/display/JENKINS/SCM+Sync+configuration+plugin).

CI-palvelimelle on asennettu PostgreSQL, mutta siellä ei ajeta tietokantaserveriä palveluna. 
Sen sijaan kullekin buildille on määritelty oma tietokantaportti parametrilla `-DargLine=-Ddb.port=5678`, 
jolloin testiajon käynnistyessä käynnistetään uusi tietokantaserveri, jonka datahakemisto on build-hakemiston alla
ja joka palvelee omassa portissaan buildin ajan.

## Loggaus

Koski loggaa tapahtumia kolmeen erilliseen logitiedostoon

1. `koski-audit.log` eli Audit-logi sisältää kaikki tärkeät käyttäjätapahtumat, kuten login, tietojen haut hakuehtoineen, tietojen katselu, lisäys ja muokkaus. Logista ilmenee aina käyttäjän OID ja IP-osoite.
2. `koski-access.log` eli Access-logi sisältää kaikki palvelimen käsittelemät HTTP-pyynnöt polkuineen, paluukoodeineen ja käsittelyaikoineen.
3. `koski-performance.log` eli performanssilogi sisältää krittisten operaatioiden kuten tietokantakyselyiden ja rajapintakutsujen käsittelyaikoja.
4. `koski.log` eli "sovelluslogi" sisältää kehitys- ja diagnostiikkatietoa, kuten kaikki virheilmoitukset

Kaikkien logien tapahtumat siirretään testiympäristön palvelimilta Filebeat-agentilla Elasticsearch -tietokantaan, josta ne ovat katseltavissa Kibana-käyttöliittymän avulla.

Loggaus on konfiguroitu tiedostolla `log4j.properties`, joka määrittää loggauksen kehitysympäristössä (tuotanto- ja kehitysympäristöjen lokitus määritellään toisessa repositoriossa sijaitsevilla konfiguraatiotiedostoilla). Tämän konfiguraatiotiedoston avulla määritellään esimerkiksi se, mitä logataan mihin tiedostoon. Kuten konfiguraatiotiedostosta ilmenee, tapahtuu access-loggaus ohjaamalla Jettyn `RequestLog`-luokan logitus `koski-access.log` -tiedostoon. Vastaavasti `fi.vm.sade.auditlog.Audit` -luokan loggaus ohjataan tiedostoon `koski-audit.log` ja `fi.oph.koski.util.Timer` -luokan loggaus tiedostoon `koski-performance.log`. Kaikki muu menee sovelluslogiin `koski.log`.

Koski-sovelluskoodissa audit-loggaus tehdään `AuditLog`-luokan kautta ja sovellusloggaus käyttäen `Logging`-luokkaa, jolta sovelluskoodi saa käyttöönsä loggerin, joka automaattisesti liittää logiviesteihin käyttäjä- ja IP-osoitetiedot.

## Testiympäristö

### URLit

Testiympäristön Koski löytyy täältä:

    https://dev.koski.opintopolku.fi//koski/

Ympäristöön kuuluvat Opintopolku-palvelun osat, esimerkiksi henkilöpalvelu:

    https://dev.koski.opintopolku.fi/authentication-service/swagger/index.html

Testiympäristö käyttää tuotannon ePerusteet-palvelua

    https://eperusteet.opintopolku.fi/

Koodistopalvelua käytetään toistaiseksi Opintopolun QA-ympäristöstä

    https://testi.virkailija.opintopolku.fi/koodisto-ui/html/index.html#/etusivu

### Sovelluksen asennus pilviympäristöön

Jotta voit asentaa sovelluksen pilviympäristöön, tarvitset erillisen ympäristörepositorion, jossa on ympäristöjen konfiguraatiot. Pyydä apua kehitystiimiltä!

- Aseta `CLOUD_ENV_DIR` ympäristömuuttuja osoittamaan ympäristörepositorion polkuun
- Valitse haluamasi ympäristö sourcaamalla jokin ympäristöskripti ympäristörepositorion `tenants`-hakemistosta.

#### Lokaalin version asentaminen

Ajamalla

    make dist version=local
    
muodostuu uusi lokaali asennuspaketti applikaatiosta. Asennuspakettiin tulee mukaan kaikki lokaalisti kommitoidut muutokset.

Tämän jälkeen voit asentaa Koskesta uuden version pilviympäristöön ajamalla

    make deploy version=local
    
Paketin muodostamisen ja asennuksen voi hoitaa myös yhdellä komennolla

    make dist deploy version=local
    
#### Versioidun paketin asentaminen

Ajamalla

    make dist version=<versio>
    
muodostuu uusi versio applikaatiosta. Applikaatio siirretään Artifactoryyn ja versiohallintaan lisätään uusi tägi annetulla versionumerolla.
Asennuspakettiin tulee mukaan kaikki lokaalisti kommitoidut muutokset.

Tämän jälkeen voit asentaa Koskesta uuden version pilviympäristöön ajamalla

    make deploy version=<versio>
        
Paketin muodostamisen ja asennuksen voi hoitaa myös yhdellä komennolla

    make dist deploy version=<versio>
    

### Pilviasennuksen operoiminen

Ks. https://github.com/Opetushallitus/koski-env
    
## Build-prosessi ja hakemistot

Paikallisesti ajettaessa Jetty lataa resurssit hakemistosta [target/webapp] jonka sisältyy muodostuu webpack-buildilla, ks [webpack.config.js](web/webpack.config.js), joka muun muassa kopioi staattisia resursseja paikoilleen
hakemistosta [web] ja sen alihakemistoista.

Staattisista tiedostoista palvellaan vain `web.xml` -tiedostossa erikseen määritellyt polut. 
Tietyt polut ohjataan palvelemaan etusivun sisältö, ks. [ScalatraBootstrap](src/main/scala/ScalatraBootstrap.scala) ja [IndexServlet](src/main/scala/fi/oph/koski/servlet/IndexServlet.scala). 

Versioitu paketti tehdään kopioimalla versionhallinnassa olevat tiedostot hakemistoon [target/build] ja buildaamalla applikaatio uudelleen siellä (ks [scripts/dist.sh]. War-pakettiin päätyy siis lopulta [target/build/target/webapp] -hakemiston sisältö.

## Toteutus ja integraatiot

### Konfigurointi

Sovellus käyttää konfigurointiin [Typesafe Config](https://github.com/typesafehub/config) -kirjastoa, 
jonka avulla tarvittavat asetukset haetaan tiedostoista ja/tai komentoriviltä.

Sovelluksen oletusasetukset ovat tiedostossa [src/main/resources/reference.conf]. 
Kun sovellus käynnistetään ilman ulkoisia parametrejä, käynnistyy se näillä asetuksilla 
ja toimii "kehitysmoodissa", eli käynnistää paikallisen tietokannan, 
eikä ota yhteyttä ulkoisiin järjestelmiin.

Tuotantokäytössä ja testiympäristössä käytetään asetuksia, joilla Koski saadaan ottamaan yhteys ulkoisiin
järjestelmiin. Pilviympäristössä käytetään tällä hetkellä [cloud/restart.sh] -skriptiä, jolla annetaan
tarvittavat asetukset.

Kehityskäytössä voit käyttää erilaisia asetuksia tekemällä asetustiedostoja, kuten vaikkapa [src/main/resources/koksidev.conf]
(ei versionhallinnassa, koska sisältää luottamuksellista tietoa) ja antaa käytettävän tiedoston nimi käynnistysparametrina, 
esim. `-Dconfig.resource=koskidev.conf`. Valmiita asetustiedostoja voi pyytää kehitystiimiltä.

### Henkilötiedot

Koski ei tallenna henkilötietoja omaan kantaansa, vaan hakee/tallentaa ne Opintopolun [henkilöpalveluun](https://github.com/Opetushallitus/henkilo). [toteutus](src/main/scala/fi/oph/koski/oppija/OppijaRepository.scala)

Kun Koskessa haetaan henkilön tietoja esimerkiksi sukunimellä, haetaan lista mahdollisista henkilöistä ensin henkilöpalvelusta, jonka jälkeen se [suodatetaan](src/main/scala/fi/oph/koski/opiskeluoikeus/OpiskeluoikeusRepository.scala#L8)
Koskessa olevien opinto-oikeuksien perusteella.

Käyttäjä voi nähdä vain ne opinto-oikeudet, jotka liittyvät oppilaitokseen, johon hänellä on käyttöoikeus. Henkilön organisaatioliitokset ja käyttöoikeudet haetaan [henkilöpalvelusta](https://github.com/Opetushallitus/henkilo) ja [organisaatiopalvelusta](https://github.com/Opetushallitus/organisaatio). [toteutus](src/main/scala/fi/oph/koski/user/RemoteUserRepository.scala)

Esimerkkihaku: haetaan organisaatiopuurakenne.

    https://testi.virkailija.opintopolku.fi:443/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&suunnitellut=true&lakkautetut=false&&&&&&oid=1.2.246.562.10.50822930082&
    
Henkilöpalvelun swagger:

    https://virkailija.tordev.tor.oph.reaktor.fi/authentication-service/swagger/index.html

### ePerusteet

Tällä hetkellä Koskeen voi tallentaa vain [ePerusteista](https://eperusteet.opintopolku.fi/) löytyvien tutkintojen tietoja. Opinto-oikeutta lisättäessa lista mahdollisista tutkinnoista haetaan
ePerusteista ja [Opinto-oikeuden](src/main/scala/fi/oph/koski/opiskeluoikeus/Opiskeluoikeus.scala) sisältämään [tutkinto](src/main/scala/fi/oph/koski/tutkinto/Tutkinto.scala)-osioon tallennetaan tieto ePerusteet-linkityksestä.

EPerusteista haetaan myös tutkinnon hierarkkinen [rakenne](src/main/scala/fi/oph/koski/tutkinto/TutkintoRakenne.scala), joka kuvaa, mistä tutkinnon osista tutkinto koostuu. [toteutus](https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/eperusteet/RemoteEPerusteetRepository.scala)

EPerusteiden Swagger-dokumentaatio:

    https://eperusteet.opintopolku.fi/eperusteet-service/

Pari testiurlia:

    https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet?nimi=Ty%C3%B6njoh
    https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/1013059
    https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/1013059/kaikki
    https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/diaari?diaarinumero=104/011/2014

### Koodistopalvelu

Koski käyttää [Koodistopalvelua](https://github.com/Opetushallitus/koodisto) mm. tutkintoihin liittyvien arviointiasteikkojen hakemiseen.

Koodistopalvelun Swagger-dokumentaatio:

    https://testi.virkailija.opintopolku.fi/koodisto-service/swagger/index.html

Pari testiurlia Koodistopalveluun:

    https://testi.virkailija.opintopolku.fi/koodisto-service/rest/codes/arviointiasteikkoammatillinenhyvaksyttyhylatty/1
    https://testi.virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/arviointiasteikkoammatillinenhyvaksyttyhylatty/1
    
Koski osaa tarvittaessa luoda käytettävät koodistot ja koodistopalveluun. Käynnistä parametrillä `-Dkoodisto.create=true`. 

### LDAP

Kosken käyttäjäautentikaatio on toteutettu Opintopolku-järjestelmän LDAPia vasten. LDAP-palvelimen osoite ja tunnukset konfiguroidaan `ldap.host`, `ldap.userdn` ja `ldap.password` -asetuksilla.

### REST-endpointit

Taulukossa tärkeimmät Koski-palvelun käyttämät ulkoiset REST-endpointit

| URL                                                                   | Käyttötarkoitus                                        |
|-----------------------------------------------------------------------|--------------------------------------------------------|
| `GET /authentication-service/resources/henkilo?no=true&count=0&q=${query}`  | Oppijan haku nimellä tai hetulla (oid, nimi, hetu) |
| `POST /authentication-service/resources/s2s/koski/henkilotByHenkiloOidList`  | Oppijoiden haku oid-listan perusteella (oid, nimi, hetu, äidinkieli, kansalaisuudet) |
| `POST /authentication-service/resources/s2s/koski/henkilo` | Oppijan luonti tai päivitys |
| `GET /authentication-service/resources/s2s/koski/kayttooikeusryhmat/${oid}` | Käyttäjän käyttöoikeusryhmien haku organisaatioittain (organisaatio-oid, ryhmä-id) |
| `GET /organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&oid=${oid}` | Organisaatiohiearkian haku (oid, nimi, oppilaitoskoodi, organisaatiotyypit, oppilaitostyyppi, aliorganisaatiot) |
| `GET /organisaatio-service/rest/organisaatio/v2/hae?aktiiviset=true&lakkautetut=false&searchStr=${searchTerm}` | Organisaation hakeminen oppilaitosnumerolla (oid, nimi, oppilaitoskoodi, organisaatiotyypit, oppilaitostyyppi, aliorganisaatiot) |
| `GET /koodisto-service/rest/codeelement/codes/${koodisto.koodistoUri}/${koodisto.versio} ` | Koodiston koodien haku (koodiuri, koodiarvo, nimi, lyhytnimi, kuvaus, versio, voimassaalkupvm) |
| `GET /koodisto-service/rest/codes/${koodisto} ` | Koodiston tietojen haku (koodistouri, versio, nimi, kuvaus, koodistoryhmä, voimassaalkupvm) |

## Rajapinta-dokumentaatio

Koski-järjestelmän rajapinta-dokumentaatio generoidaan lähdekoodista sekä testidatasta ja esimerkiksi testiympäristön dokumentaatio löytyy osoitteesta
    
https://koskidev.koski.oph.reaktor.fi/koski/documentation
    
JSON-scheman visualisointiin on käytetty json-schema-viewer nimistä kirjastoa, johon on tehty joitakin Koski-projektin vaatimia muutoksia. 
Kirjaston lähdekoodi löytyy Opetushallituksen GitHub-repositoriosta

https://github.com/Opetushallitus/json-schema-viewer

Mon Apr 18 14:18:11 EEST 2016
