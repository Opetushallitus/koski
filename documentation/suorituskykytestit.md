# Koski suorityskykytestit

Suorituskykytestit ovat ajossa Github Actionsissa ja ne ajetaan joka yö testiopintopolkua vasten.

Ajantasaisen tiedon testien ajamisesta saa [github actionsin configuraatiosta](../.github/workflows/run_performance_tests.yml).

## Käytettävä testidata

### Koski

Kosken testeissä testidata generoidaan automaattisesti testien yhteydessä. Datan generointiin voi tutustua
[koodista katsomalla insertereitä](../src/test/scala/fi/oph/koski/perftest/).

### Valpas

Koska Valppaassa oleellinen osa kokonaisuutta on haku- ja valintapalvelusta tuleva tieto, pitää Valppaan
suorituskykytestejä varten luoda Valppaan hakuihin soveltuvia opiskeluoikeuksia, jotka on linkitetty
haku- ja valintapalvelusta löytyvään dataan. Linkitys tapahtuu oppija oideilla. 2021 testiympäristöön tuotiin
tuotantoa vastaava data, jolloin noin 60000 oppijan oideja vastaavat 9. luokan opiskeluoikeudet
luotiin QA-ympäristössä Koskeen .

Kyseiset oidit löytyvät [testien resursseista](../src/test/resources/valpas_qa_oppija_oidit.txt).

Huom! Jos muutat kyseisiä oideja, niin suorituskykytestien listanäkymäkyselyiden testidata pitää päivittää myös.
Ohjeet siihen ovat [erillisessä READMEssä](../src/test/resources/readme.md).

#### Datan lisääminen testiympäristöissä Koskeen

Koska Valppaassa näkyy kerrallaan vain yhden vuoden oppijat, tulee Koskessa olevat opiskeluoikeudet data päivittää
testiympäristöön 30.9. Tämän voi tehdä esimerkiksi seuraavasti:

* Poista data suoraan Kosken tietokannasta seuraavalla sql:llä:

```sql
delete from opiskeluoikeus where koulutusmuoto = 'perusopetus' and alkamispaiva = '2021-08-15' and paattymispaiva = '2022-06-04' and luokka ='9A' and versionumero = 1 and aikaleima > '2021-10-10';
```

* Päivitä opiskeluoikeuksiin lisättävät alkamis- ja loppumispäivämäärät
[opiskeluoikeuden luovaan koodiin](../src/test/scala/fi/oph/koski/perftest/ValpasOpiskeluoikeusInserterScenario.scala).

* Aja uusi data testiympäristöön seuraavasti:

```
export KOSKI_USER="XXXXXX"
export KOSKI_PASS="XXXXXX"
export KOSKI_BASE_URL="https://koski.testiopintopolku.fi/koski"
export VIRKAILIJA="https://virkailija.testiopintopolku.fi"
export PERFTEST_ROUNDS=68190 # oidien määrä tiedostossa valpas_qa_oppija_oidit.txt, käytä tarkkaa määrää
export KOSKI_SERVER_COUNT=2
export PERFTEST_THREADS=10
export WARMUP_ROUNDS=0
export KOSKI_VALPAS_ORGANISAATIOT_FILENAME="valpas_qa_peruskoulujen_oidit.txt"
export KOSKI_VALPAS_OPPIJAOIDIT_FILENAME="valpas_qa_oppija_oidit.txt"

mvn test-compile
mvn exec:java -Dexec.mainClass="fi.oph.koski.perftest.ValpasPeruskouluFromOidsOpiskeluoikeusInserter"
```

#### Kaiken datan näkyminen Valppaassa

Koska Valpas perustuu pitkälti oppivelvollisuuteen, 18 vuoden jälkeen oppijoiden tietoja ei normaalisti näy Valppaassa.
Testaamisen mahdollistamiseksi löytyy Valppaan konfiguraatioista seuraavat parametrit, jotka ovat QA ympäristössä
asetettuna niin, että kaikkien oppijoiden pitäisi listautua Valppaassa ja sitä kautta heidän haku- ja
valintapalvelussa olevat tiedot haetaan testien yhteydessä. QA:lla asetukset ovat:

```
valpas = {
  rajapäivät {
    lakiVoimassaVanhinSyntymäaika = "1800-01-01"
    oppivelvollisuusLoppuuIkä = 218
    maksuttomuusLoppuuIkä = 220
```
