# Todennetun Osaamisen Rekisteri (TOR)

Todennetun osaamisen rekisteri (TOR) tulee toimimaan kattavana opetustoimialan tietovarantona, joka tarjoaa
tutkintoon johtavat suoritustiedot eri koulutusasteilta. Yleinen TOR-dokumentaatio kootaan CSC:n wikiin: https://confluence.csc.fi/display/OPHPALV/Todennetun+osaamisen+rekisteri

Tässä git-repositoriossa on TOR-järjestelmän ohjelmakoodi, tietokannan rakennuslausekkeet ja tekninen dokumentaatio ohjelmistokehitystä varten.

TOR on [EUPL](LICENSE.txt)-lisensoitu sovellus, josta on mahdollista käynnistää kehitysinstanssi omalla työasemalla, alla olevien kehitysohjeiden mukaisesti. TOR-sovellus on alustariippumaton, sillä se pyörii Java-virtuaalikoneella. Kehitysympäristö toimii sellaisenaan ainakin Linux ja OSX-käyttöjärjestelmissä.

Huom! TOR-järjestelmän kehitys on vielä alkuvaiheessa ja kaikki tässä dokumentissa kuvattu voi vielä muuttua matkan varrella.

## Käsitteet

Keskeiset entiteetit, ja järjestelmät, joihin nämä tallennetaan.

| käsite         | selite                                       | tunniste         | tallennuspaikka        |
|----------------|----------------------------------------------|------------------|------------------------|
| Koodisto       | Kooditus objekteille, esim tutkintonimikkeet | id (tekstiä)     | Koodistopalvelu        |
| Koodi          | Yksittäisen objektin koodi koodistossa       | id (tekstiä)     | Koodistopalvelu        |
| Oppija         | Opiskelija, oppilas.                         | henkilöOid       | Henkilöpalvelu         |
| Organisaatio   | Oppilaitos, kunta, eri rooleissa             | organisaatioOid  | Organisaatiopalvelu    |
| Opinto-oikeus  | Oppijan suhde oppilaitokseen ja suoritettavaan tutkintoon (tutkinto, oppija, oppilaitos, voimassaoloaika, läsnäolotiedot...) | id (numeerinen)  | TOR        |
| Peruste        | Tutkinnon tai tutkinnon osan peruste         | diaarinumero     | ePerusteet             |
| Suoritus       | Oppijan suoritus (tutkinto, oppija, oppilaitos, aika...) | id (numeerinen)  | TOR        |
| Tutkinto       | Tutkinnon kuvaus (tutkintotunnus, nimi...)   | tutkintotunnus   | Koodisto               |

## Teknologiat

Nämä ovat keskeiset TOR-järjestelmässä käytettävät teknologiat. Lista kuvaa järjestelmän nykytilaa ja muuttuu matkan varrella
tarpeen mukaan.

- PostgreSQL 9.4 -tietokanta
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

- Git
- GNU Make
- Maven 3 (osx: `brew install maven`)
- Postgres (osx: `brew install postgres`)
- Tekstieditori (kehitystiimi käyttää IntelliJ IDEA 14)

## Paikallinen PostgreSQL-tietokanta

Kehityskäyttöön tarvitaan paikallinen PostgreSQL-tietokanta. TOR-sovellus luo paikallisen kannan, skeeman ja käyttäjän
automaattisesti ja käynnistää myös tarvittavan PostgreSQL-serveriprosessin.

Paikallisen kannan konfiguraatio on tiedostossa `postgresql/postgresql.conf` ja tietokannan datahakemisto on `postgresql/data`.

Jos haluat pitää Postgresin käynnissä erikseen, voit käynnistää sen komentoriviltä komennolla

    make postgres

PostgreSQL jää pyörimään konsoliin ja voit sammuttaa sen painamalla ctrl-c.

Käynnistyessään Tor-sovellus huomaa, jos tietokanta on jo käynnissä, eikä siinä tapauksessa yritä käynnistää sitä.

Kehityksessä käytetään kahta kantaa: `tor` jota käytetään normaalisti ja `tortest` jota käytetään automaattisissa
testeissä (tämä kanta tyhjennetään aina testiajon alussa). Molemmat kannat sisältävät `tor` -skeeman, ja sijaitsevat
fyysisesti samassa datahakemistossa.


### SQL-yhteys paikalliseen kantaan

Jos ja kun haluat tarkastella paikallisen kehityskannan tilaa SQL-työkalulla, se onnistuu esimerkiksi Postgren omalla komentorivityökalulla `psql`:

    psql tor tor
    psql tortest tor

Peruskomennot

    \dt    listaa taulut
    \q     poistuu psql:stä

Sitten vaikka

    select * from arviointi;

### Kantamigraatiot

Skeema luodaan flywayllä migraatioskripteillä, jotka ovat hakemistossa `src/main/resources/db/migration`.

Tor-sovellus ajaa migraatiot automaattisesti käynnistyessään.

## Buildi

TOR:n buildiin kuuluu frontin buildaus npm:llä ja serverin buildaus Mavenilla. Tätä helpottamaan on otettu käyttöön `make`, jonka avulla
eri taskit on helppo suorittaa. Ks `Makefile`-tiedosto.

Buildaa koko systeemi

    make build    
    
Buildaa frontti, ja buildaa automaattisesti kun tiedostoja muokataan:

    make watch

## TOR-sovelluksen ajaminen kehitystyöasemalla

Aja JettyLauncher-luokka IDEAsta/Eclipsestä, tai käynnistä TOR vaihtoehtoisesti komentoriviltä

    make build
    make run

Avaa selaimessa

    http://localhost:7021/tor

Suoritus-testidatat näkyy

    http://localhost:7021/tor/suoritus/
    
## Ajaminen paikallisesti käyttäen ulkoisia palveluja (esim henkilöpalvelu)

Ilman parametrejä ajettaessa TOR käyttää mockattuja ulkoisia riippuvuuksia.

Ottaaksesi käyttöön ulkoiset integraatiot, kuten henkilpalvelun, voit antaa TOR:lle käynnistysparametrinä käytettävän konfiguraatiotiedoston sijainnin. Esimerkiksi

    -Dconfig.resource=qa.conf
    
Tällä asetuksella käytetään tiedostoa `src/main/resources/qa.conf`. Tämä tiedosto ei ole versionhallinnassa, koska se sisältää ei-julkista tietoa.
            
## Testit

Buildaa ja aja kaikki testit

    make test
    
Kun applikaatio pyörii paikallisesti (ks. ohjeet yllä), voi Mocha-testit ajaa selaimessa osoitteessa

    http://localhost:7021/tor/test/runner.html

Mocha-testit voi ajaa myös nopeasti komentoriviltä

    make fronttest
        


## Asennus pilveen (CSC:n ePouta)

Ennakkovaatimukset:

1. Sinulla on tunnus CSC:n cPouta-ympäristöön
2. Poudan ympäristö(muuttuja)määrittely: https://pouta.csc.fi/dashboard/project/access_and_security/api_access/openrc/ on ladattu ja käytössä (`source Project_2000079-openrc.sh`)
3. Sinun julkinen ssh avain on lisättynä tänne: https://github.com/reaktor/oph-poutai-env/tree/master/roles/ssh.init/files/public_keys (ja koneiden konfiguraatio on päivitetty)
4. Käytössäsi on Ruby 2.0 tai uudempi

Tämän jälkeen voit pushata uuden version TOR:sta ajamalla,

    make deploy

jonka jälkeen uusin versio pyörii testiympäristössä: 

    http://tordev.tor.oph.reaktor.fi/tor/
    
Lokien katsominen onnistuu komennolla:

    make tail
    
## Testiympäristö

Testiympäristön TOR löytyy täältä:

    http://tordev.tor.oph.reaktor.fi/tor/
    
Ympäristöön kuuluvat Opintopolku-palvelun osat täällä:

    https://srv2.va-dev.oph.fi/
    
Esimerkiksi henkilöpalvelu:


    https://srv2.va-dev.oph.fi/authentication-service/swagger/index.html


## Henkilöpalvelu-integraatio

TOR ei tallenna henkilötietoja omaan kantaansa, vaan hakee/tallentaa ne Opintopolun [henkilöpalveluun](https://github.com/Opetushallitus/henkilo).

## ePerusteet-integraatio

Swagger:

    https://eperusteet.opintopolku.fi/eperusteet-service/
    
Pari testiurlia:
    
    https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet?nimi=Ty%C3%B6njoh
    https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/1013059
    
## Organisaatiopalvelu-integraatio

[Organisaatiopalvelusta](https://github.com/Opetushallitus/organisaatio) haetaan käyttäjän organisaatiopuu, jonka perusteella päätellään, mitkä suoritukset voidaan näyttää.

Esimerkkihaku:

    https://testi.virkailija.opintopolku.fi:443/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&suunnitellut=true&lakkautetut=false&&&&&&oid=1.2.246.562.10.50822930082&
