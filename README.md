# Koski

Koski tulee toimimaan kattavana opetustoimialan tietovarantona, joka tarjoaa
tutkintoon johtavat suoritustiedot eri koulutusasteilta. Yleinen Koski-dokumentaatio kootaan CSC:n wikiin:

> https://confluence.csc.fi/display/OPHPALV/Koski

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
| Opiskeluoikeus | Oppijan suhde oppilaitokseen ja suoritettavaan tutkintoon (tutkinto, oppija, oppilaitos, voimassaoloaika, läsnäolotiedot...) | id (numeerinen)  | Koski        |
| Peruste        | Tutkinnon tai tutkinnon osan peruste         | diaarinumero     | ePerusteet             |
| Suoritus       | Oppijan suoritus (tutkinto, oppija, oppilaitos, aika...) | id (numeerinen)  | Koski        |
| Tutkinto       | Tutkinnon kuvaus (tutkintotunnus, nimi...)   | tutkintotunnus   | Koodisto               |

## Teknologiat

Nämä ovat keskeiset Koski-järjestelmässä käytettävät teknologiat. Lista kuvaa järjestelmän nykytilaa ja muuttuu matkan varrella
tarpeen mukaan.

- PostgreSQL 9.6 -tietokanta
- Elasticsearch 5.1.1 -hakuindeksi
- Palvelinteknologiat
  - Scala 2.12 -ohjelmointikieli ja -kääntäjä
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
- Java 8 (osx: `brew cask install java`)
- Maven 3 (osx: `brew install maven`)
- Postgres 9.6 (osx: `brew install postgresql`)
- Elasticsearch 5.1.1 (osx: `brew install elasticsearch`)
- Elasticsearch analysis-icu plugin (`elasticsearch-plugin install analysis-icu`)
- Node.js ja NPM (osx: `brew install node`)
- Tekstieditori (kehitystiimi käyttää IntelliJ IDEA 14/15)

## Buildi ja ajaminen

Koski:n buildiin kuuluu frontin buildaus (npm / webpack) ja serverin buildaus Mavenilla. Tätä helpottamaan on otettu käyttöön `make`, jonka avulla
eri taskit on helppo suorittaa. Ks [`Makefile`](Makefile)-tiedosto.

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

> [http://localhost:7021/koski](http://localhost:7021/koski)

Selaimeen avautuu login-sivu, josta pääset eteenpäin käyttäjätunnuksella "kalle". Salasana on sama kuin käyttäjätunnus.

Näin ajettuna sovellus käyttää paikallista PostgreSQL-kantaa ja Elasticsearch-indeksiä, jotka se myös itse käynnistää. Sovellus ei myöskään käytä mitään ulkoisia palveluja. Sillä on siis turvallista leikkiä.

### Ajaminen paikallisesti käyttäen ulkoisia palveluja (esim henkilöpalvelu)

Ilman parametrejä ajettaessa Koski käyttää mockattuja ulkoisia riippuvuuksia.

Ottaaksesi käyttöön ulkoiset integraatiot, kuten henkilpalvelun, voit antaa Koski:lle käynnistysparametrinä käytettävän konfiguraatiotiedoston sijainnin. Esimerkiksi

    -Dconfig.resource=qa.conf

Tällä asetuksella käytetään tiedostoa `src/main/resources/qa.conf`. Tämä tiedosto ei ole versionhallinnassa, koska se sisältää ei-julkista tietoa.

## Paikallinen PostgreSQL-tietokanta

Kehityskäyttöön tarvitaan paikallinen PostgreSQL-tietokanta. Koski-sovellus luo paikallisen kannan, skeeman ja käyttäjän
automaattisesti ja käynnistää myös tarvittavan PostgreSQL-serveriprosessin.

Paikallisen kannan konfiguraatio on tiedostossa [`postgresql/postgresql.conf`](postgresql/postgresql.conf) ja tietokannan datahakemisto on [`postgresql/data`](postgresql/data).

Jos haluat pitää Postgresin käynnissä erikseen, voit käynnistää sen komentoriviltä komennolla

    make postgres

PostgreSQL jää pyörimään konsoliin ja voit sammuttaa sen painamalla ctrl-c.

Käynnistyessään Koski-sovellus huomaa, jos tietokanta on jo käynnissä, eikä siinä tapauksessa yritä käynnistää sitä.

Kehityksessä käytetään kahta kantaa: `koski` jota käytetään normaalisti ja `koskitest` jota käytetään automaattisissa
testeissä (tämä kanta tyhjennetään aina testiajon alussa). Molemmat kannat sisältävät `koski` -skeeman, ja sijaitsevat
fyysisesti samassa datahakemistossa.

## Paikallinen Elasticsearch-kanta

Koski-sovellus käynnistää paikallisen Elasticseach-serverin käynnistyessään, ellei serveriä löydy portista 9200.

Jos haluat pitää Elasticsearching käynnissä erikseen, voit käynnistää sen komentoriviltä komennolla

    make elastic

Paikallisen kannan datat tulevat hakemistoon `elastic-data`.

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

> [http://localhost:7021/koski/test/runner.html](http://localhost:7021/koski/test/runner.html)

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

Koski merkitsee tapahtumia erillisiin logitiedostoihin:

1. `koski-audit.log` eli Audit-logi sisältää kaikki tärkeät käyttäjätapahtumat, kuten login, tietojen haut hakuehtoineen, tietojen katselu, lisäys ja muokkaus. Logista ilmenee aina käyttäjän OID ja IP-osoite.
2. `koski-access.log` eli Access-logi sisältää kaikki palvelimen käsittelemät HTTP-pyynnöt polkuineen, paluukoodeineen ja käsittelyaikoineen.
3. `koski-performance.log` eli performanssilogi sisältää krittisten operaatioiden kuten tietokantakyselyiden ja rajapintakutsujen käsittelyaikoja.
4. `koski-ip-tracking.log` eli IP-seurantalogi sisältää tiedonsiirtoon oikeutettujen käyttäjien IP-osoitteiden muutokset.
5. `koski.log` eli "sovelluslogi" sisältää kehitys- ja diagnostiikkatietoa, kuten kaikki virheilmoitukset.

Kaikkien logien tapahtumat siirretään testiympäristön palvelimilta Filebeat-agentilla Elasticsearch -tietokantaan, josta ne ovat katseltavissa Kibana-käyttöliittymän avulla.

Loggaus on konfiguroitu tiedostolla `log4j.properties`, joka määrittää loggauksen kehitysympäristössä (tuotanto- ja kehitysympäristöjen lokitus määritellään toisessa repositoriossa sijaitsevilla konfiguraatiotiedostoilla). Tämän konfiguraatiotiedoston avulla määritellään esimerkiksi se, mitä logataan mihin tiedostoon. Kuten konfiguraatiotiedostosta ilmenee, tapahtuu access-loggaus ohjaamalla Jettyn `RequestLog`-luokan logitus `koski-access.log` -tiedostoon. Vastaavasti `fi.vm.sade.auditlog.Audit`-luokan loggaus ohjataan tiedostoon `koski-audit.log`, `fi.oph.koski.tiedonsiirto.IPTracking`-luokan loggaus tiedostoon `koski-ip-tracking.log` ja `fi.oph.koski.util.Timer` -luokan loggaus tiedostoon `koski-performance.log`. Kaikki muut logit menevät tiedostoon `koski.log`.

Koski-sovelluskoodissa audit-loggaus tehdään `AuditLog`-luokan kautta ja sovellusloggaus käyttäen `Logging`-luokkaa, jolta sovelluskoodi saa käyttöönsä loggerin, joka automaattisesti liittää logiviesteihin käyttäjä- ja IP-osoitetiedot.

## Testiympäristö

### Palvelut

Kuvaus | URL | Muuta
--------|-----|-------
Koski | [hallinta-ui](https://dev.koski.opintopolku.fi/koski/) [api][koski-api] [pulssi-ui](https://dev.koski.opintopolku.fi/koski/pulssi) |
Vanha henkilöpalvelu | [palvelukortti](https://confluence.csc.fi/pages/viewpage.action?pageId=46204851) [api](https://dev.koski.opintopolku.fi/authentication-service/swagger/index.html) | Koski käyttää vain [henkilo](https://dev.koski.opintopolku.fi/authentication-service/swagger/index.html#!/henkilo)-resurssia tiedonsiirron virheen lähettämiseksi organistaatiolle.
Oppijanumerorekisteri | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/Oppijanumerorekisteri) [api](https://dev.koski.opintopolku.fi/oppijanumerorekisteri-service/swagger-ui.html) | Oppijan haku oid:lla tai hetulla. Uuden oppijan luonti.
Käyttöoikeuspalvelu | [palvelukortti](https://confluence.csc.fi/pages/viewpage.action?pageId=68725146) [api](https://dev.koski.opintopolku.fi/kayttooikeus-service/swagger-ui.html) | Käyttäjän käyttöoikeusryhmien haku.
Organisaatiopalvelu | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/Organisaatiotietojen+hallintapalvelu) [api](https://dev.koski.opintopolku.fi/organisaatio-service/swagger/index.html) | Organisaation tai -hierarkian haku.
Koodistopalvelu | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/Koodistopalvelu) [api][koodisto-api] [hallinta-ui](https://testi.virkailija.opintopolku.fi/koodisto-ui/html/index.html#/etusivu) | Koodien ja metatietojen haku ja luonti.
ePerusteet | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/ePerusteet) [api][eperusteet-api] [api-dokumentaatio](https://confluence.csc.fi/display/oppija/ePerusteet+julkiset+rajapinnat) [ui](https://eperusteet.opintopolku.fi/) | Tuotantoversio
Viestintä / Ryhmäsähköposti | [palvelukortti](https://confluence.csc.fi/pages/viewpage.action?pageId=65923709) [api](https://testi.virkailija.opintopolku.fi/ryhmasahkoposti-service/swagger/index.html) |

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

Paikallisesti ajettaessa Jetty lataa resurssit hakemistosta `target/webapp` jonka sisältyy muodostuu webpack-buildilla, ks [webpack.config.js](web/webpack.config.js), joka muun muassa kopioi staattisia resursseja paikoilleen
hakemistosta [`web/`](web/) ja sen alihakemistoista.

Staattisista tiedostoista palvellaan vain `web.xml` -tiedostossa erikseen määritellyt polut.
Tietyt polut ohjataan palvelemaan etusivun sisältö, ks. [ScalatraBootstrap](src/main/scala/ScalatraBootstrap.scala) ja [IndexServlet](src/main/scala/fi/oph/koski/servlet/IndexServlet.scala).

Versioitu paketti tehdään kopioimalla versionhallinnassa olevat tiedostot hakemistoon [target/dist] ja buildaamalla applikaatio uudelleen siellä (ks [scripts/dist.sh]. War-pakettiin päätyy siis lopulta [target/dist/target/webapp] -hakemiston sisältö.

## Toteutus ja integraatiot

### Konfigurointi

Sovellus käyttää konfigurointiin [Typesafe Config](https://github.com/typesafehub/config) -kirjastoa,
jonka avulla tarvittavat asetukset haetaan tiedostoista ja/tai komentoriviltä.

Sovelluksen oletusasetukset ovat tiedostossa [`src/main/resources/reference.conf`](src/main/resources/reference.conf).
Kun sovellus käynnistetään ilman ulkoisia parametrejä, käynnistyy se näillä asetuksilla
ja toimii "kehitysmoodissa", eli käynnistää paikallisen tietokannan,
eikä ota yhteyttä ulkoisiin järjestelmiin.

Tuotantokäytössä ja testiympäristössä käytetään asetuksia, joilla Koski saadaan ottamaan yhteys ulkoisiin
järjestelmiin. Pilviympäristössä käytetään tällä hetkellä `cloud/restart.sh` -skriptiä, jolla annetaan
tarvittavat asetukset.

Kehityskäytössä voit käyttää erilaisia asetuksia tekemällä asetustiedostoja, kuten vaikkapa `src/main/resources/koskidev.conf`
(ei versionhallinnassa, koska sisältää luottamuksellista tietoa) ja antaa käytettävän tiedoston nimi käynnistysparametrina,
esim. `-Dconfig.resource=koskidev.conf`. Valmiita asetustiedostoja voi pyytää kehitystiimiltä.

### Oppijanumerorekisteri, organisaatiopalvelu ja käyttöoikeuspalvelu

Koski ei tallenna henkilötietoja omaan tietokantaansa, vaan hakee ja tallentaa ne Opintopolun [oppijanumerorekisteriin](https://confluence.csc.fi/display/OPHPALV/Oppijanumerorekisteri) ([toteutus](src/main/scala/fi/oph/koski/henkilo/AuthenticationServiceClient.scala)).

Kun käyttäjä hakee henkilön tietoja esimerkiksi sukunimellä, hakee Koski listan mahdollisista henkilöistä ensin oppinumerorekisteristä, jonka jälkeen Koski suodattaa hakutuloksen Koskessa olevien opinto-oikeuksien perusteella ([toteutus](src/main/scala/fi/oph/koski/henkilo/HenkilötiedotFacade.scala)).

Käyttäjä voi nähdä vain ne opinto-oikeudet, jotka liittyvät oppilaitokseen, johon hänellä on käyttöoikeus. Koski hakee henkilön organisaatioliitokset [organisaatiopalvelusta](https://confluence.csc.fi/display/OPHPALV/Organisaatiotietojen+hallintapalvelu) ja käyttöoikeudet [käyttöoikeuspalvelusta](https://confluence.csc.fi/pages/viewpage.action?pageId=68725146).

Esimerkki [organisaatiohierarkian](https://testi.virkailija.opintopolku.fi/organisaatio-service/swagger/index.html#!/organisaatiov2/searchOrganisaatioHierarkia) hakemisesta:

    curl -X GET --header 'Accept: application/json' 'https://testi.virkailija.opintopolku.fi/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&suunnitellut=true&lakkautetut=false&oid=1.2.246.562.10.50822930082'

### Koodistopalvelu

Koski käyttää [Koodistopalvelua](https://github.com/Opetushallitus/koodisto) mm. tutkintoihin liittyvien arviointiasteikkojen hakemiseen.

Testiurleja ([api][koodisto-api]):

> https://testi.virkailija.opintopolku.fi/koodisto-service/rest/codes/arviointiasteikkoammatillinenhyvaksyttyhylatty/1
> https://testi.virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/arviointiasteikkoammatillinenhyvaksyttyhylatty/1

Koski osaa tarvittaessa luoda käytettävät koodistot ja koodistopalveluun. Käynnistä parametrillä `-Dkoodisto.create=true`.

### ePerusteet

Tällä hetkellä Koskeen voi tallentaa vain [ePerusteista](https://eperusteet.opintopolku.fi/) löytyvien tutkintojen tietoja. Opiskeluoikeutta lisättäessa lista mahdollisista tutkinnoista haetaan
ePerusteista ja opiskeluoikeuden ([toteutus](src/main/scala/fi/oph/koski/schema/Opiskeluoikeus.scala)) sisältämään tutkinto-osioon tallennetaan tieto ePerusteet-linkityksestä.

ePerusteista haetaan myös tutkinnon hierarkkinen rakenne ([toteutus](src/main/scala/fi/oph/koski/tutkinto/TutkintoRakenne.scala)), joka kuvaa, mistä tutkinnon osista tutkinto koostuu.

Integraation [toteutus](src/main/scala/fi/oph/koski/eperusteet/RemoteEPerusteetRepository.scala).

Testiurleja ([api][eperusteet-api]):

> https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet?nimi=Ty%C3%B6njoh
> https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/1013059
> https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/1013059/kaikki
> https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/diaari?diaarinumero=104/011/2014

### LDAP

Kosken käyttäjäautentikaatio on toteutettu Opintopolku-järjestelmän LDAPia vasten. LDAP-palvelimen osoite ja tunnukset konfiguroidaan `ldap.host`, `ldap.userdn` ja `ldap.password` -asetuksilla.

### Virta ja YTR

Koski osaa hakea oppijoiden tietoja kahdesta ulkoisesta järjestelmästä: CSC:n [Virrasta](https://confluence.csc.fi/display/VIRTA/VIRTA-opintotietopalvelu) ([api-dokumentaatio](https://confluence.csc.fi/display/VIRTA/WS-rajapinta)) ja Ylioppilastutkintorekisteristä.

## Rajapinta-dokumentaatio

Koski-järjestelmän [rajapinta-dokumentaatio][koski-api] generoidaan lähdekoodista sekä testidatasta.

JSON-scheman visualisointiin on käytetty json-schema-viewer nimistä kirjastoa, johon on tehty joitakin Koski-projektin vaatimia [muutoksia](https://github.com/Opetushallitus/json-schema-viewer).

[koski-api]: https://dev.koski.opintopolku.fi/koski/documentation
[koodisto-api]: https://dev.koski.opintopolku.fi/koodisto-service/swagger/index.html
[eperusteet-api]: https://eperusteet.opintopolku.fi/eperusteet-service/
