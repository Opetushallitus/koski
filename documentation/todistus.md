# Todistus – digitaalinen todistus

Todistusmoduuli tuottaa digitaalisia, allekirjoitettuja PDF-todistuksia. Ensimmäinen
toteutettu todistustyyppi on **Yleisen kielitutkintojen todistus (YKI-todistus)**.

---

## Arkkitehtuuri

### Backend

```
Servletit
├── TodistusApiServlet       – kansalaisen/OPH-ylläpitäjän routet (status, generate)
├── TodistusDownloadServlet  – PDF-lataus (suora tai presigned S3-URL)
└── TodistusPreviewServlet   – HTML-esikatselu (OPH-ylläpitäjälle)

Taustaprosessit
├── TodistusScheduler        – poimii jonosta töitä ja ajaa ne
└── TodistusCleanupScheduler – hoitaa vanhentuneita ja epäonnistuneita (esim. kontin kaatuminen) töitä

Ydinpalvelu
└── TodistusService          – koordinoi kokonaisen todistuksen luonnin

Tietokanta ja S3
├── TodistusJobRepository    – todistus-jobit (PostgreSQL). Tietokannassa taulu todistus_job .
└── TodistusResultRepository – PDF-tiedostot (S3 / paikallisesti Localstack)

PDF-tuotanto
└── pdfgenerator/
    └── TodistusPdfGenerator – Thymeleaf-template → HTML → PDF (OpenHtmlToPdf)

Allekirjoitus
└── swisscomclient/
    ├── SwisscomClient       – PDF-allekirjoituksen lisäys ja koordinointi
    ├── RemoteSwisscomClient – HTTP-yhteys Swisscom AIS -palveluun
    ├── MockSwisscomClient   – HTTP-mock
    └── SwisscomCRLAndOCSPExtender – CRL/OCSP-tiedot allekirjoitettuun PDFihin

Allekirjoituksen analysointi
├── PdfSignatureAnalyzer        – PDF-allekirjoitusrakenteen analysointi (ByteRange, PKCS#7, DSS)
├── PdfSignatureAnalyzerCLI     – komentorivityökalu PDF-allekirjoitusten analysointiin
└── scripts/analyze-pdf-signature.sh – shell-skripti analyzer-työkalun käynnistämiseen

Todistustyyppikohtainen logiikka
└── yleinenkielitutkinto/
    ├── YleinenKielitutkintoTodistusDataBuilder – opiskeluoikeus → template-data
    └── YleinenKielitutkintoTodistusData        – template-data-malli

Validointi
└── TodistusDataValidation  – tarkistaa template-datan ennen PDF-tuottamista
```

### Frontend

Kansalainen, huoltaja ja OPH:n työntekijä pääsevät lataamaan todistukset yleisen kielitutkinnon
käyttöliittymässä.

```
web/app/kielitutkinto/
├── YleinenKielitutkintoEditor.tsx         – YKI-opiskeluoikeuden muokkausnäkymä
└── YleinenKielitutkintoTodistusLataus.tsx – todistuksen lataus + tilannekuva

web/app/components-v2/todistus/
└── Todistus.less  – todistuslataus-komponentin tyylit. Huom: Käytössä myös YO-todistuksen latauksessa YTR:stä.
```

### Todistus-templatet ja resurssit

```
src/main/resources/todistus-templates/
├── kielitutkinto_yleinenkielitutkinto_fi.html          – suomenkielinen template (digitaalinen)
├── kielitutkinto_yleinenkielitutkinto_fi_tulostettava_uusi.html     – suomenkielinen tulostus-template (uusi, 3 sivua)
├── kielitutkinto_yleinenkielitutkinto_fi_tulostettava_paivitys.html – suomenkielinen tulostus-template (päivitys, 2 sivua)
├── kielitutkinto_yleinenkielitutkinto_sv.html          – ruotsinkielinen (WIP)
├── kielitutkinto_yleinenkielitutkinto_sv_tulostettava_uusi.html     – ruotsinkielinen tulostus uusi (WIP)
├── kielitutkinto_yleinenkielitutkinto_sv_tulostettava_paivitys.html – ruotsinkielinen tulostus päivitys (WIP)
├── kielitutkinto_yleinenkielitutkinto_en.html          – englanninkielinen (WIP)
├── kielitutkinto_yleinenkielitutkinto_en_tulostettava_uusi.html     – englanninkielinen tulostus uusi (WIP)
├── kielitutkinto_yleinenkielitutkinto_en_tulostettava_paivitys.html – englanninkielinen tulostus päivitys (WIP)
├── fonts/   – fonttien source-tiedostot (base64-enkoodattu templateihin)
└── images/  – Todistuksella näkyvien logojen source-SVG-tiedostot (base64-enkoodattu templateihin)

src/main/mockdata/todistus/
└── *.pdf  -esimerkkitiedostoja, joissa PDF:ää muokattu allekirjoittamisen jälkeen yms.
```


---

## Todistusjobin tilat (state machine, tallessa tietokannassa)

```
QUEUED
  └─► GATHERING_INPUT      – haetaan oppijan ja OO:n nykytiedot, lasketaan hash henkilötiedoista
        └─► GENERATING_RAW_PDF  – template-data rakennetaan ja validoidaan, PDF tuotetaan
              └─► SAVING_RAW_PDF      – allekirjoittamaton PDF → S3
                    └─► STAMPING_PDF        – PDF allekirjoitetaan Swisscom AIS ja validoidaan
                          └─► SAVING_STAMPED_PDF – allekirjoitettu PDF → S3
                                └─► COMPLETED
```

STAMPING_PDF ja SAVING_STAMPED_PDF skipataan käytettäessä printtileiskoja.

**Huom:** STAMPING_PDF-vaiheessa suoritetaan myös allekirjoituksen kryptografinen validointi (jos konfiguroitu). Jos validointi epäonnistuu, job siirtyy ERROR-tilaan ja virheellinen PDF tallennetaan debuggausta varten.

Virhetilaan siirtynyt job saa tilan `ERROR`. Keskeytynyt job (`INTERRUPTED`)
palautetaan jonoon uudestaan. Vanhentuneet jobit asetetaan tilaan `EXPIRED`.

### Jobien uudelleenkäyttö

`TodistusJobRepository.addOrReuseExisting()` käyttää CTE-kyselyä, joka
palauttaa olemassa olevan jobin jos sen `template_variant`, OO-versio ja henkilötiedot-hash
vastaavat uutta pyyntöä. Uusi job luodaan vain jos aiempia ei löydy tai
ne ovat ERROR-tilassa. Esim. jobit `fi` ja `fi_tulostettava_uusi` ovat erillisiä ja niitä
ei käytetä toinen toisen tilalla.

## Pääsynhallinta

| Toiminto                                              | Kansalainen   | Huoltaja            | OPH-pääkäyttäjä | OPH Kielitutkintojen katselija |
|-------------------------------------------------------|---------------|---------------------|---|--------------------------------|
| Digitaalisten todistusten luonti- ja lataus           | Omasta OO:sta | Huollettavan OO:sta | Kaikista | Kaikista                       |
| Tulostettavien todistusten luonti- ja lataus          | -             | -                   | Kaikista | Kaikista                       |
| Presigned-URL todistuksen lataamiseksi suoraan S3:sta | –             | –                   | Kaikista | -                              |
| HTML-esikatselu                                       | –             | –                   | Kaikista | -                              |

Todistuksen lataus onnistuu vain jos opiskeluoikeus on vahvistettu.

### Tulostus-variantit

Tulostus-variantit (`fi_tulostettava_uusi`, `fi_tulostettava_paivitys`, `sv_tulostettava_uusi`, `sv_tulostettava_paivitys`, `en_tulostettava_uusi`, `en_tulostettava_paivitys`)
ovat sallittu vain OPH-pääkäyttäjälle ja OPH-kielitutkintojen katselijalle.

## Allekirjoitus (Swisscom AIS)

`SwisscomClient`-luokassa. Allekirjoitusprosessi:

1. PDF valmistetaan allekirjoitukseen (`prepareForSigning`).
2. SHA-256-hash lähetetään Swisscom AIS -palveluun.
3. Palvelu palauttaa digitaalisen allekirjoituksen sekä CRL- ja OCSP-tiedot.
4. Allekirjoitus ja peruutustiedot (CRL/OCSP) lisätään PDFiin (`SwisscomCRLAndOCSPExtender`).

Swisscom-konfiguraatio (avaimet, keystore) haetaan tuotannossa AWS Secrets
Managerita, lokaalissa kehitysympäristössa niitä ei käytetä, vaan vastaukset on Mock:ssa kovakoodattu.

Jos lokaalisti käynnistää esim. testin ympäristömuuttujalla "SWISSCOM_SECRET_ID=swisscom-secrets", haetaan
salaisuudet DEV:n Secrets managerista, jotta allekirjoittamista voi testata lokaalisti. Tämä vaatii, että
SSO-sessio AWS:ään on voimassa.

Swisscom-salaisuuksien hallinta, ks.: https://github.com/Opetushallitus/koski-aws-infra/tree/master/tools/certs

### DocMDP-rajoitukset

Allekirjoitukseen ei lisätä DocMDP (Document Modification Detection and Prevention) -suojausta, koska se estää pitkäaikaisen validointitiedon (LTV - Long-Term Validation) lisäämisen allekirjoituksen jälkeen. DocMDP-suojaus lukitsee dokumentin muutoksilta, mutta PAdES-LT-standardin mukainen allekirjoitus vaatii OCSP- ja CRL-tietojen lisäämisen Document Security Store (DSS) -rakenteeseen allekirjoituksen jälkeen.

Tekninen syy: DocMDP sallii vain rajoitetut muutokset (approval signatures, form fill-in, comments), mutta DSS-rakenteen lisääminen vaatii laajempia muutoksia PDF:ään. Tämä tekee mahdottomaksi luoda validia PAdES-LT-allekirjoitusta DocMDP:n kanssa.

Lisätietoja: https://stackoverflow.com/questions/79837411/is-it-possible-to-create-a-valid-pades-lt-certification-signature-with-docmdp-le

## PDF-tuotanto

`TodistusPdfGenerator`:

1. Thymeleaf-template renderöidään HTML:ksi template-muuttujilla.
2. HTML muunnetaan PDFiksi OpenHtmlToPdf-kirjastolla.
3. PDF-metadataan lisätään: oppijaOid, opiskeluoikeusOid, jobId, versio,
   git-commit-hash ja opiskeluoikeusJson.

Fontit (Open Sans) ja logot on base64-enkoodattu suoraan HTML-templatetiedostoihin. Tämä on tehty, jotta
templaten voi itsensä avata itsenäisenä tiedostona selaimeen esikatselua varten.

## Taustaprosessit

### TodistusScheduler

Vastaa todistusjobien suorittamisesta. Poimii jonottavia töitä `takeNext()`-kutsulla.

### TodistusCleanupScheduler

Vastaa jonojen ylläpidosta:
- **Orvot** (`findOrphanedJobs`): job on joko INTERRUPTED tai jobia hoitanut kontti ei ole
  enää aktiivinen. Palautetaan jonoon enintään 3 kertaa, sen jälkeen ERROR.
- **Vanhentumat** (`findExpiredJobs`): COMPLETED-työt vanhentuvat
  konfiguroidun ajan jälkeen ja siirtyvät tilaan EXPIRED. S3-bucket on tarkoitus
  konffatta poistamaan tiedostot aikaisintaan yhtä vanhoina.

---

## Lokalisointi

Todistuksen template ja sitä täyttävä `YleinenKielitutkintoTodistusDataBuilder`
käyttävät `KoskiLocalizationRepository`-ta kaikissa käyttäjälle näkyvissä
olevissa teksteissä. Tuettut kieli-koodit: `fi`, `sv`, `en`. Kieli
johdetetaan `template_variant`-kentästä `TodistusTemplateVariant.baseLanguage()`-metodilla
(esim. `fi_tulostettava_uusi` → `fi`). Ruotsin- ja englanninkieliset templatet ovat vielä työ alla.


## Saavutettavuus

PDF:ssä on templatessa mukana bookmarkit käytön helpottamiseksi. Tämän ajatellaan auttavan, koska tekstisisällön
lukujärjestys ei ole optimaalinen: toisen todistussivun sisältö tulee ennen allekirjoitustietoja.
Tämä johtuu openhtmltopdf:n rajoituksista: kaikkien sivujen alareunaan sijoitettu sisältö tulee
lukujärjestyksessä koko dokumentin lopussa.

## Muuta

- Henkilötiedot-hash lasketaan kentiltä: `etunimet|sukunimi|syntymäaika`.
  Jos hash on muuttunut edellisestä todistuksesta, luodaan uusi todistus.
- `TodistusPreviewServlet` palauttaa HTML:n (ei allekirjoitettu PDF);
  virkailijakäyttäjä voi käyttää sitä esikatseluun.

---
# Testaus

```
src/test/scala/fi/oph/koski/todistus/
├── TodistusSpecHelpers           – yhteinen fixture, HTTP-apumetodit, scheduler-kontrolli
├── TodistusWorkflowSpec          – state machine, jobien uudelleenkäyttö, vanheneminen
├── TodistusKayttoOikeudetSpec    – pääsynhallinnan yksityiskohtia
├── TodistusAuditLogSpec          – audit-loggauksen testaus
├── TodistusLatausSpec            – PDF-sisältö, metadata, allekirjoitus, PDF/UA-vaatimukset,
│                                   pikselivertailu referenssikuviin verraten
├── PdfSignatureAnalyzerSpec      – PDF-allekirjoitusrakenneanalysaattorin testit. ByteRange-validointi,
│                                   PKCS#7-rakenne, DSS-validointi, OCSP/CRL-tiedot.
├── TodistusDataValidationSpec    – template-datan validointi
└── YleinenKielitutkintoTodistusDataBuilderSpec – data-transformaatio, järjestys, lokalisaatio
```

Pikselivertailu käyttää referenssikuvia hakemistosta
`src/test/resources/todistus-test-kielitutkinto-yleinenkielitutkinto-fi-expected/`.

`simulateMockDownloadError()`:ssa on simulointi todistuksen luonnin epäonnistumiseen tietyllä
mock-oppijalla.

```
web/test/e2e/
└── kielitutkinnontodistus.spec.ts    - Playwright e2e-testit
```

## Suorituskykytestit (Performance Tests)

Todistuksen luonnin suorituskykyä testataan erillisillä perftesteillä, jotka mittaavat koko asynkronisen työnkulun (generate → poll → download) suorituskykyä.

```
src/test/scala/fi/oph/koski/perftest/
├── YleinenKielitutkintoOppijatGenerator – Luo testidataa (oppijat YKI-suorituksilla)
├── RandomYkiOpiskeluoikeusOid           – Apuluokka OID-iterointiin
└── YleinenKielitutkintoTodistusGenerator – Varsinainen suorituskykytesti
```

### Testidatan generointi

Testit käyttävät ennalta luotuja oppija-tietoja, jotka on tallennettu tiedostoon `src/test/resources/yki_perftest_opiskeluoikeus_oids.txt` ja versionhallinnassa.

#### Ensimmäinen kerta: Testidatan luominen ja lisääminen repositoryyn

1. **Generoi testdata lokaalisti tai CI:ssä**:

```bash
# Lokaalisti
export KOSKI_USER=käyttäjä
export KOSKI_PASS=salasana
export KOSKI_BASE_URL=http://localhost:7021/koski
export YKI_PERFTEST_OPPIJA_COUNT=200

mvn exec:java -Dexec.mainClass="fi.oph.koski.perftest.YleinenKielitutkintoOppijatGenerator"
```

Generointi luo oppijat satunnaisilla:
- Kielillä (FI, EN, ES, IT, SE, FR, SV, RU, DE)
- Tutkintotasoilla (pt, kt, yt)
- Osasuorituksilla (4 kpl, arvosanat vastaavat tasoa)

**Oppijamäärän valinta:**
- Oppijamäärän tulee olla riittävän suuri, jotta sama (oppija, variantti) -yhdistelmä ei toistu liian usein
samassa testiajossa
- Jos sama yhdistelmä toistuu, todistus-jobi käytetään uudelleen → aikamittaukset vääristyvät

**Jos generoit CI:ssä**: GitHub Actions tallentaa tiedoston artifactiksi perffitestien ajon yhteydessä.
Lataa artifactit ja lisää siellä oleva tiedosto gitiin, `src/test/resources/yki_perftest_opiskeluoikeus_oids.txt`.

#### Jatkossa

Tiedosto on versionhallinnassa, joten:
- **CI:ssä**: Käytetään tiedostossa olevia opiskeluoikeus-oideja.
- **Lokaalisti**: Tiedosto pitää korvata paikallisesti generoidulla, ellei ole ajamassa testejä QA:ta vasten.

### Suorituskykytestin ajaminen

Testi simuloi todellista käyttöä:
1. **95% pyyntöjä** digitaalisille varianteille (fi, sv, en)
2. **5% pyyntöjä** tulostettaville varianteille (fi/sv/en_tulostettava_uusi/paivitys)

```bash
# Aja paikallisesti pienellä määrällä
export KOSKI_USER=pää
export KOSKI_PASS=pää
export KOSKI_BASE_URL=http://localhost:7021/koski
export PERFTEST_THREADS=2
export PERFTEST_ROUNDS=5
export WARMUP_ROUNDS=2

mvn exec:java -Dexec.mainClass="fi.oph.koski.perftest.YleinenKielitutkintoTodistusGenerator"
```

### Konfiguraatioparametrit

| Ympäristömuuttuja | Oletusarvo | Kuvaus |
|-------------------|------------|---------|
| `PERFTEST_THREADS` | 5 | Rinnakkaisten säikeiden määrä |
| `PERFTEST_ROUNDS` | 10 | Iteraatioiden määrä per säie |
| `WARMUP_ROUNDS` | 5 | Lämmittelykierrosten määrä (ei mukana tilastoissa) |
| `PERFTEST_SUCCESS_THRESHOLD_PERCENTAGE` | 95 | Vaadittu onnistumisprosentti (%) |
| `PERFTEST_MIN_THROUGHPUT` | 0.5 | Vaadittu läpäisykyky (ops/sec) |
| `YKI_TODISTUS_POLL_INTERVAL_MS` | 2000 | Job-statuksen kyselyväli (ms) |
| `YKI_TODISTUS_MAX_WAIT_MS` | 300000 | Maksimi odotusaika (5 min) |
| `YKI_TODISTUS_DIGITAL_WEIGHT` | 95 | Digitaalisten varianttien osuus (%) |
| `YKI_TODISTUS_OIDS_FILE` | `src/test/resources/...` | Polku OID-tiedostoon |

### GitHub Actions

Suorituskykytesti ajetaan automaattisesti osana yöllisiä perftestejä (`.github/workflows/run_performance_tests.yml`):

**Huom:** Tulostettavat variantit ovat nopeampia, koska niitä ei allekirjoiteta (ei STAMPING_PDF-vaihetta).

## Allekirjoituksen verifointi

PDF-allekirjoitusten validointia voi suorittaa itse tehdyllä **PDF Signature Analyzer** -työkalulla, joka suorittaa sekä rakenteellisen että kryptografisen validoinnin. Tarkemmat tiedot työkalusta alla. Samaa analysaattoria käytetään myös automaattitesteissä ja tuotantoympäristön allekirjoitusvalidoinnissa.

Lisäksi allekirjoituksen voi tarkistaa manuaalisilla työkaluilla, joista tarkempia tietoja alla erillisessä kappaleessa.

Hakemistossa `src/main/mockdata/todistus` on erilaisia manuaalitestitapauksia.

### PDF Signature Analyzer -työkalu

Koski sisältää työkalun PDF-allekirjoitusten analysointiin. Työkalu suorittaa sekä rakenteellisen että kryptografisen validoinnin.

**Työkalu validoi:**

- **ByteRange-analyysi**: Tarkistaa allekirjoitetun ja allekirjoittamattoman sisällön osuudet
- **PKCS#7-rakenne**: Analysoi allekirjoituksen SignedData-rakenteen, sertifikaattiketjun ja aikaleiman
- **Kryptografinen allekirjoituksen validointi**: Varmistaa että sisällön hash-arvo vastaa allekirjoituksessa olevaa
- **DSS-validointi**: Varmistaa Document Security Store -rakenteen (OCSP/CRL-tiedot, VRI-merkinnät)

**HUOM! Työkalu ei tarkista sertifikaatin luottamusketjua (chain of trust) eikä sertifikaatin voimassaoloaikaa.**

#### Käyttö komentoriviltä

```bash
# Shell-skriptillä
./scripts/analyze-pdf-signature.sh todistus.pdf

# Suoraan Maven:llä
mvn exec:java \
  -Dexec.mainClass="fi.oph.koski.todistus.PdfSignatureAnalyzerCLI" \
  -Dexec.args="todistus.pdf"
```

Exit-koodit:
- `0` - PDF on validi
- `1` - Virheelliset parametrit
- `2` - PDF:ssä on ongelmia
- `3` - Analyysi epäonnistui

### Manuaalinen verifointi

Allekirjoituksen kryptografisen validiteetin ja sertifikaattiketjun voi varmentaa:
- **Adobe Acrobat** - näyttää allekirjoituksen tilan ja sertifikaattidetaljit
- **DVV:n työkalu** - https://dvv.fi/en/validate-document

## Saavutettavuuden testaus

Saavutettavuutta on manuaalitestattu Adobe Acrobat:lla, millä näkee bookmarkit side bar:ssa, sekä käyttämällä
Windows-virtuaalikoneessa Parallels:lla ajettua PAC-työkalua (https://pac.pdf-accessibility.org/en).
