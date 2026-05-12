# Koski suorituskykytestit

Suorituskykytestit ovat ajossa Github Actionsissa ja ne ajetaan joka yö testiopintopolkua vasten.

Ajantasaisen tiedon testien ajamisesta saa [github actionsin configuraatiosta](../.github/workflows/run_performance_tests.yml).

## Koski testidata

Kosken testeissä testidata generoidaan automaattisesti testien yhteydessä. Datan generointiin voi tutustua
[koodista katsomalla insertereitä](../src/test/scala/fi/oph/koski/perftest/).

## Valpas testidata

Koska Valppaassa oleellinen osa kokonaisuutta on haku- ja valintapalvelusta tuleva tieto, pitää testidatassa olla
oppijoita, joilla on haku- ja valintatuloksia. Tätä varten tulee testidataa luoda niin, että:
1. Oppijalla on Koskessa oppivelvollisuuden suorittamiseen liittyvä opiskeluoikeus, voidaan luoda itse insertereillä, kts. ohjeet alempaa.
1. Haku- ja valintapalvelu Ovarassa on haku- ja valintatuloksia oppijoille.
1. Oppijanumerorekisterissä on oppijoille kotikuntahistoriat, tämä vaaditaan että oppijat näkyvät Valppaassa.

Koska Valppaassa näkyy kerrallaan vain yhden vuoden oppijat, tulee Koskessa olevat opiskeluoikeudet päivittää
testiympäristöön 30.9. Lisäksi oppijat tulee päivittää jos haku- ja valintapalvelussa oppijoiden datat muuttuvat.

### Testidatan tallennuspaikka

Valppaan suorituskykytestit testaavat kahta eri näkymää: yksittäisen oppijan sivua sekä oppilaitosten listanäkymää.
Yksittäisen oppijan näkymän testi käyttää pohjadatana listausta oppija OIDeja. Oppilaitoksen listanäkymä käyttää
pohjadatana mappausta oppilaitoksesta oppija OIDeihin, joilla on kyseisessä oppilaitoksessa opiskeluoikeus.

Oppija-OIDit ja oppilaitos-oppija-mappaus tallennetaan S3-bucketiin `valpas-perf-test-oppija-oids-qa`.
Bucket sisältää kaksi CSV-tiedostoa, joiden nimet konfiguroidaan GitHub-secreteillä `VALPAS_OPPIJAOIDIT_S3_KEY`
ja `VALPAS_ORGANISAATIOT_S3_KEY` (ks. [CI-konfiguraatio](#ci-ympäristön-konfiguraatio)):

- Oppija-OIDit (`VALPAS_OPPIJAOIDIT_S3_KEY`, oletus `valpas_qa_oppija_oidit.csv`) — header `master_oid`,
  yksi OID per rivi.
  - Tämä tiedosto pyydetään Ovaran ylläpitäjiltä ja ladataan sitten Kosken S3-bucketiin, josta se on saatavilla Oppijanumerorekisterin ylläpitäjille kotikuntahistorioiden luontia varten.
- Oppilaitos-oppija-mappaus (`VALPAS_ORGANISAATIOT_S3_KEY`, oletus `valpas_qa_peruskoulujen_ja_oppijoiden_oidit.csv`)
  — header `oppilaitos_oid,oppija_oidit`, yksi rivi per oppilaitos, oppija-OIDit välilyönnillä erotettuna
  toisessa sarakkeessa
  - Tämä tiedosto voidaan muodostaa itse, kun testioppijoille on ensin luotu sopivat opiskeluoikeudet.

Nämä tiedostot ladataan manuaalisesti S3:een.
Suorituskykytestit lukevat tiedostot suoraan S3:sta OIDC-autentikoinnilla (ks. [CI-konfiguraatio](#ci-ympäristön-konfiguraatio)).

Paikallisesti suorituskykytestit ja inserter lukevat myös tiedostot suoraan S3:sta. Ne käyttävät paikallista AWS SSO kirjautumista autentikointiin.

### Valpas testidatan päivittäminen

#### 1. Vanhan datan poisto QA-ympäristön kannasta

Mahdollinen vanha data on poistettava QA-ympäristöstä ennen kuin uusia datoja voidaan lisätä.

Tarkista [opiskeluoikeuden luovasta koodista](../src/test/scala/fi/oph/koski/perftest/ValpasOpiskeluoikeusInserterScenario.scala)
mitä arvoja `alkamispäivä`, `valmistumispäivä` ja `luokka` -muuttujilla on. Tällä hetkellä arvot ovat:

- `alkamispaiva`: `2025-08-15`
- `paattymispaiva`: `2026-05-31`
- `luokka`: `9A`

Ensin on hyvä asettaa kyselyn aikakatkaisu tarpeeksi suureksi:

```sql
SET statement_timeout TO 7200000; -- 7200 sekuntia = 2 tuntia
```

Vanha data poistetaan QA:n operatiivisesta tietokannasta (`koski`):

```sql
DELETE FROM opiskeluoikeus
WHERE koulutusmuoto = 'perusopetus'
  AND alkamispaiva = '2025-08-15'
  AND paattymispaiva = '2026-05-31'
  AND luokka = '9A';
```

Huom! QA:n tietokannassa on edelleen vanhempaa testidataa:
- kausi 2021–2022, ei poistettu testiaineistomuutosten takia
- kausi 2024-08-15–2025-05-31, luokka `9P`, 3 oppilaitosta
- 2025-05 on luotu uudet datat DVV:n pysyvästä testiaineistosta löytyville oppijoille päivämäärätiedoilla

#### 2. Oppija-OIDien päivittäminen

Jos oppija-OIDit muuttuvat:

- Lataa uusi oppija-OID CSV-tiedosto S3-bucketiin `valpas-perf-test-oppija-oids-qa` GitHub-secretin
  `VALPAS_OPPIJAOIDIT_S3_KEY` mukaisella nimellä
- CSV-muoto: header `master_oid`, yksi OID per rivi

#### 3. Päivämäärien päivittäminen koodissa

Päivitä opiskeluoikeuksiin lisättävät alkamis- ja päättymispäivämäärät
[opiskeluoikeuden luovaan koodiin](../src/test/scala/fi/oph/koski/perftest/ValpasOpiskeluoikeusInserterScenario.scala).

#### 4. Uuden datan ajo testiympäristöön

Inserter lukee oppija-OIDit S3:sta (`VALPAS_OPPIJAOIDIT_S3_KEY`) ja oppilaitos-OIDit paikallisesta tiedostosta
(`src/test/resources/` + `KOSKI_VALPAS_ORGANISAATIOT_FILENAME`). Paikallinen oppilaitos-OID-tiedosto sisältää yhden
oppilaitos-OIDin per rivi ilman headeria.

Tee ensin AWS login.
```bash
aws sso login --sso-session <sso session name in local aws config>
```

Aja inserter sitten seuraavasti. **Huom!** päivitä `PERFTEST_ROUNDS` vastaamaan oppija-OID-tiedoston rivien tarkkaa
määrää (ilman headeria):

```bash
export KOSKI_USER="XXXXXX"
export KOSKI_PASS="XXXXXX"
export KOSKI_BASE_URL="https://koski.testiopintopolku.fi/koski"
export VIRKAILIJA="https://virkailija.testiopintopolku.fi"
export PERFTEST_ROUNDS=75000  # Oppija OIDien tarkka määrä csv-tiedostossa, eli rivien määrä ilman headeria
export KOSKI_SERVER_COUNT=2
export PERFTEST_THREADS=10
export WARMUP_ROUNDS=0
export KOSKI_VALPAS_ORGANISAATIOT_FILENAME="valpas_qa_peruskoulujen_oidit.txt"
export VALPAS_OPPIJAOIDIT_S3_KEY="<oppija-OID-csv-tiedoston nimi S3:ssa>"

mvn test-compile
mvn exec:java -Dexec.mainClass="fi.oph.koski.perftest.ValpasPeruskouluFromOidsOpiskeluoikeusInserter"
```

Inserter kirjoittaa ajon päätyttyä tiedoston `valpas_qa_peruskoulujen_ja_oppijoiden_oidit.csv` nykyiseen
hakemistoon. Tiedosto sisältää insertin aikana muodostuneen oppilaitos-oppija-mappauksen.

#### 5. Tulostiedoston lataaminen S3:een

Lataa insertin tuottama CSV-tiedosto manuaalisesti S3-bucketiin `valpas-perf-test-oppija-oids-qa`
GitHub-secretin `VALPAS_ORGANISAATIOT_S3_KEY` mukaisella nimellä:

Jos insertin automaattinen CSV-generointi epäonnistuu, voidaan oppilaitos-oppija-mappaus generoida
vaihtoehtoisesti raportointikantaan ajettavalla SQL-kyselyllä insertin jälkeen (edellyttää raportointikannan
regenerointia). Kysely ajetaan raportointikantaa (`raportointikanta`) vasten:

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
            -- Tähän riville kaikki oppija-oidit välilyönnillä erotettuna:
                '1.2.246.562.24.50770631005 1.2.246.562.24.10001511889 1.2.246.562.24.10001665997 ...',
                ' '
            )) AS oppija_oid
    )
SELECT
    oppilaitos_oid,
    string_agg(DISTINCT r_opiskeluoikeus.oppija_oid, ' ') AS oppija_oidit
FROM oppilaitokset
JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppilaitos_oid = oppilaitokset.oppilaitos_oid
JOIN loytyvat_oppijat ON r_opiskeluoikeus.oppija_oid = loytyvat_oppijat.oppija_oid
GROUP BY oppilaitos_oid;
```

Kyselyn voi luoda myös ajamalla `node src/test/resources/valpas_qa_peruskoulujen_ja_oppijoiden_query_export.js`,
kun oppijoiden ja oppilaitosten oid:t on tallennettu paikallisesti tiedostoihin `valpas_qa_peruskoulujen_oidit.txt` ja `valpas_qa_oppija_oidit.txt`.

Kyselyn tulos muotoillaan CSV-muotoon (header `oppilaitos_oid,oppija_oidit`) ja ladataan S3:een
`VALPAS_ORGANISAATIOT_S3_KEY`:n mukaisella nimellä.

### CI-ympäristön konfiguraatio

Ajantasaisen tiedon CI-ajojen parametreista (threadit, roundit, jne.) saa
[github actionsin konfiguraatiosta](../.github/workflows/run_valpas_performance_tests.yml).
