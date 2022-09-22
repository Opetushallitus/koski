# Koski suorityskykytestit

Suorituskykytestit ovat ajossa Github Actionsissa ja ne ajetaan joka yö testiopintopolkua vasten.

Ajantasaisen tiedon testien ajamisesta saa [github actionsin configuraatiosta](../.github/workflows/run_performance_tests.yml).

## Koski testidata

Kosken testeissä testidata generoidaan automaattisesti testien yhteydessä. Datan generointiin voi tutustua
[koodista katsomalla insertereitä](../src/test/scala/fi/oph/koski/perftest/).

## Valpas testidata

Koska Valppaassa oleellinen osa kokonaisuutta on haku- ja valintapalvelusta tuleva tieto, pitää testidatassa olla
oppijoita, joilla on haku- ja valintatuloksia. Tätä varten tulee testidataa luoda niin, että oppijalla on Koskessa
oppivelvollisuuden suorittamiseen liittyvä opiskeluoikeus (oppija löytyy Valppaasta) ja haku- ja valintapalvelussa
haku- ja valintatuloksia.

Koska Valppaassa näkyy kerrallaan vain yhden vuoden oppijat, tulee Koskessa olevat opiskeluoikeudet data päivittää
testiympäristöön 30.9. Lisäksi oppijat tulee päivittää jos haku- ja valintapalvelussa oppijoiden datat muuttuvat.

### Valpas testidatan päivittäminen

#### 1. Vanhan datan poisto QA-ympäristön kannasta

Vanha data on poistettava QA-ympäristöstä, ennen kuin datoja voidaan päivittää.

Kyselyssä `alkamispaiva`, `paattymispaiva` ja `luokka` -kentissä käytetty seuraavanlaisia arvoja:

- `X` on opiskeluoikeuden alkamispäivä, joka on muotoa `YYYY-08-15`, on määritelty [opiskeluoikeuden luovaan koodiin](../src/test/scala/fi/oph/koski/perftest/ValpasOpiskeluoikeusInserterScenario.scala).
- `Y` on opiskeluoikeuden päättymispäivä, joka on muotoa `YYYY-06-04`, on määritelty [opiskeluoikeuden luovaan koodiin](../src/test/scala/fi/oph/koski/perftest/ValpasOpiskeluoikeusInserterScenario.scala).
- `Z` on opiskeluoikeuden luokka, joka on määritelty [opiskeluoikeuden luovaan koodiin](../src/test/scala/fi/oph/koski/perftest/ValpasOpiskeluoikeusInserterScenario.scala).

Alkamis- ja päättymispäivä muodostavat yhdessä aikajakson. Esimerkiksi kauden `2021-2022` opiskeluoikeuksien aikajakso on `2021-08-05` - `2022-06-04`. Tarkista [opiskeluoikeuden luovasta koodista](../src/test/scala/fi/oph/koski/perftest/ValpasOpiskeluoikeusInserterScenario.scala) mitä arvoja kyselyssä on syytä käyttää.

Ensin on hyvä asettaa kyselyn aikakatkaisu tarpeaksi suureksi, ettei kyselyä lopeteta aikakatkaisuun:
```sql
SET statement_timeout to 7200000; -- 7200 sekuntia = 2 tuntia
```

Vanha data poistetaan QA:n tietokannasta seuraavalla kyselyllä:

```sql
-- Tarkista suorituskykytestien dokumentaatiosta, mitä arvoja alkamispäivä, päättymispäivä ja luokka tulee olla.
-- X on opiskeluoikeuden alkamispäivä, joka on muotoa YYYY-08-15 ja joka on määritelty opiskeluoikeuden luovaan koodiin.
-- Y on opiskeluoikeuden päättymispäivä, joka on muotoa YYYY-06-04 ja joka on määritelty opiskeluoikeuden luovaan koodiin.
-- Z on opiskeluoikeuden luokka, joka on määritelty opiskeluoikeuden luovaan koodiin.
DELETE FROM opiskeluoikeus WHERE koulutusmuoto = 'perusopetus' AND alkamispaiva = 'X' AND paattymispaiva = 'Y' AND luokka = 'Z';
```

#### 2. Oppija-OID:ien päivittäminen
* Mikäli haku- ja valintapalvelun datat ovat päivittyneet, lisää haku- ja valintapalvelun kehittäjiltä saadut oppijaoidit
[valpas_qa_oppija_oidit.txt](../src/test/resources/valpas_qa_oppija_oidit.txt) tiedostoon.

* Päivitä opiskeluoikeuksiin lisättävät alkamis- ja loppumispäivämäärät
[opiskeluoikeuden luovaan koodiin](../src/test/scala/fi/oph/koski/perftest/ValpasOpiskeluoikeusInserterScenario.scala).

* Aja uusi data testiympäristöön seuraavasti, HUOM! päivitä oppijoiden määrä:

```
export KOSKI_USER="XXXXXX"
export KOSKI_PASS="XXXXXX"
export KOSKI_BASE_URL="https://koski.testiopintopolku.fi/koski"
export VIRKAILIJA="https://virkailija.testiopintopolku.fi"
export PERFTEST_ROUNDS=72004 # oidien määrä tiedostossa valpas_qa_oppija_oidit.txt, käytä tarkkaa määrää
export KOSKI_SERVER_COUNT=2
export PERFTEST_THREADS=10
export WARMUP_ROUNDS=0
export KOSKI_VALPAS_ORGANISAATIOT_FILENAME="valpas_qa_peruskoulujen_oidit.txt"
export KOSKI_VALPAS_OPPIJAOIDIT_FILENAME="valpas_qa_oppija_oidit.txt"

mvn test-compile
mvn exec:java -Dexec.mainClass="fi.oph.koski.perftest.ValpasPeruskouluFromOidsOpiskeluoikeusInserter"
```

#### 3. valpas_qa_peruskoulujen_ja_oppijoiden_oidit.txt -tiedoston päivittäminen

Kun uusi data on ajettu testiympäristöön, generoi raportointikanta uudelleen.

Oidit pitää lisäksi päivittää oppilaitostensa kanssa tiedostoon
[valpas_qa_peruskoulujen_ja_oppijoiden_oidit.txt](../src/test/resources/valpas_qa_peruskoulujen_ja_oppijoiden_oidit.txt)  raportointikantaan ajettavalla sql-kyselyllä:

```sql
WITH oppilaitokset AS (
    SELECT unnest(string_to_array(
        -- Tähän riville kaikki oppilaitos-oidit välilyönnillä erotettuna:
            '1.2.246.562.10.10059380252 1.2.246.562.10.10097597065 1.2.246.562.10.102806581210 ...',
            ' '
        )) AS oppilaitos_oid
)
   , loytyvat_oppijat AS (
        SELECT unnest(string_to_array(
            -- Tähän riville kaikki oppija-oidit, jotka löytyvät Valppaasta (valpas_qa_oppija_oidit.txt) välilyönnillä erotettuna:
                '1.2.246.562.24.50770631005 1.2.246.562.24.10001511889 1.2.246.562.24.10001665997  ...',
                ' '
            )) AS oppija_oid
    )
SELECT
    concat(
            oppilaitos_oid,
            ' ',
            (SELECT
                 string_agg(DISTINCT r_opiskeluoikeus.oppija_oid, ' ')
             FROM
                 r_opiskeluoikeus
                     JOIN loytyvat_oppijat ON r_opiskeluoikeus.oppija_oid = loytyvat_oppijat.oppija_oid
             WHERE
                 oppilaitos_oid = oppilaitokset.oppilaitos_oid)
        ) AS row
FROM oppilaitokset;
```

Kyselyn voi luoda ajamalla `node src/test/resources/valpas_qa_peruskoulujen_ja_oppijoiden_query_export.js`

Tämän kyselyn tulos kopioidaan tiedostoon [valpas_qa_peruskoulujen_ja_oppijoiden_oidit.txt](../src/test/resources/valpas_qa_peruskoulujen_ja_oppijoiden_oidit.txt). Tiedosto sisältää joka rivillä ensin oppilaitoksen oidin ja sen jälkeen oppilaitoksesta haettavien oppijoiden oidit.

Ilman näitä oppija-oideja jää Sure-rajapinnan listahaut testaamatta.

### Kaiken datan näkyminen Valppaassa

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
