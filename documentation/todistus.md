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
                    └─► STAMPING_PDF        – PDF allekirjoitetaan Swisscom AIS
                          └─► SAVING_STAMPED_PDF – allekirjoitettu PDF → S3
                                └─► COMPLETED
```

Virhetilaan siirtynyt job saa tilan `ERROR`. Keskeytynyt job (`INTERRUPTED`)
palautetaan jonoon uudestaan. Vanhentunut job käsitellään
`QUEUED_FOR_EXPIRE` → `EXPIRED`.

### Jobien uudelleenkäyttö

`TodistusJobRepository.addOrReuseExisting()` käyttää CTE-kyselyä, joka
palauttaa olemassa olevan jobin jos sen `template_variant`, OO-versio ja henkilötiedot-hash
vastaavat uutta pyyntöä. Uusi job luodaan vain jos aiempia ei löydy tai
ne ovat ERROR-tilassa. Esim. jobit `fi` ja `fi_tulostettava_uusi` ovat erillisiä ja niitä
ei käytetä toinen toisen tilalla.

## Pääsynhallinta

| Toiminto | Kansalainen | Huoltaja            | OPH-pääkäyttäjä |
|---|---|---------------------|---|
| Todistuksen aloittaminen | Omasta OO:sta | Huollettavan OO:sta | Kaikista |
| Tilan tarkastelu | Omasta | Huollettavan OO:sta    | Kaikista |
| PDF-lataus | Omasta | Huollettavan OO:sta    | Kaikista |
| Presigned-URL | – | –                   | Kaikista |
| HTML-esikatselu | – | –                   | Kaikista |

Todistuksen lataus onnistuu vain jos opiskeluoikeus on vahvistettu.

### Tulostus-variantit

Tulostus-variantit (`fi_tulostettava_uusi`, `fi_tulostettava_paivitys`, `sv_tulostettava_uusi`, `sv_tulostettava_paivitys`, `en_tulostettava_uusi`, `en_tulostettava_paivitys`)
ovat sallittu vain OPH-pääkäyttäjälle. Ne näkyvät käyttöliittymässä vain
pääkäyttäjä-käyttäjille ja backend palauttaa 403-virheen jos kansalainen yrittää
käyttää tulostus-varianttia. Variantit `_uusi` ja `_paivitys` ero on template-sivumäärässä:
`_uusi` on 3-sivu template, `_paivitys` on 2-sivu template.

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
  konfiguroidun ajan jälkeen ja siirtyvät QUEUED_FOR_EXPIRE → EXPIRED. S3-bucket on tarkoitus
  konffatta poistamaan tiedostot yhtä vanhoina.

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
├── TodistusDataValidationSpec    – template-datan validointi
└── YleinenKielitutkintoTodistusDataBuilderSpec – data-transformaatio, järjestys, lokalisaatio
```

Pikselivertailu käyttää referenssikuvia hakemistosta
`src/test/resources/todistus-test-kielitutkinto-yleinenkielitutkinto-fi-expected/`.

`simulateMockDownloadError()`:ssa on simulointi todistuksen luonnin epäonnistumiseen tietyllä
mock-oppijalla.

```
web/test/e2e/
└── kielitutkinnotodistus.spec.ts    - Playwright e2e-testit
```

## Allekirjoituksen verifointi

Allekirjoituksen verifioinnille ei ole automaattitestejä. Validiutta voi varmentaa Adobe Acrobat:lla sekä
DVV:n työkalulla https://dvv.fi/en/validate-document . Hakemistossa `src/main/mockdata/todistus` on erilaisia
manuaalitestitapauksia.

## Saavutettavuuden testaus

Saavutettavuutta on manuaalitestattu Adobe Acrobat:lla, millä näkee bookmarkit side bar:ssa, sekä käyttämällä
Windows-virtuaalikoneessa Parallels:lla ajettua PAC-työkalua (https://pac.pdf-accessibility.org/en).
