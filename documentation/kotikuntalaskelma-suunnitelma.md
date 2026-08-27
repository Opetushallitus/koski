# Kotikuntalaskelma — suunnitelma

*Alustava speksi, ehdollinen VM:n (Valtiovarainministeriö) rahoitukselle. Ei vielä toteutuspäätöstä.*

## 1. Tausta

Idea lähti liikkeelle ajatuksesta, että VOS-laskenta (valtionosuuden laskenta) voisi hyötyä
mahdollisuudesta arvioida/esikatsella rahoitusta ennen varsinaista virallista laskentaprosessia
("ennakko"). Tämä ei ole uutta kelpoisuuslogiikkaa — kyse on uudesta **näkymästä olemassa olevaan
dataan**, jossa oppijat eritellään yksilötasolle tarkemman datan keräämiseksi.

Vertailukohtana käytettiin `AikuistenPerusopetusRaportti`ia (ks. `src/main/scala/fi/oph/koski/
raportit/aikuistenperusopetus/AikuistenPerusopetusRaportti.scala`), joka on jo olemassa oleva
esimerkki koulutusmuotokohtaisesta yksilötason detail-raportista, jossa on identiteettisarakkeet
(oppijaOid, oppijaMasterOid, hetu, sukunimi, etunimet) sekä rahoitussarakkeet (`rahoitukset`,
`rahoitusmuodotOk`, perustuen `opintojenRahoitus`-koodiin: koodiarvo 1 = VOS, 6 = muu kuin VOS).
Se ottaa parametreiksi yhden `oppilaitosOid`:n + `alku`/`loppu`-aikavälin — ei org-puun laajennusta
eikä yhden päivän snapshotia, toisin kuin aggregaatti-VOS-raportit (esiopetus/perusopetus/tuva/
perusopetuksen lisäopetus), jotka käyttävät `RaportitAccessResolver.kyselyOiditOrganisaatiolle`-
org-puun laajennusta ja yhtä `paiva`-parametria.

## 2. Nimi ja sijainti

**Kotikuntalaskelma**, sijoitetaan **Yleiset-raportti**-osioon (ei omaksi koulutusmuotokohtaiseksi
raportikseen). ("Oppilasraportti" oli vain kokouksessa käytetty työnimi — tiketin virallinen nimi
on Kotikuntalaskelma; koodi ja lokalisointiavaimet on nimetty sen mukaan.)

Tiketin kuvausteksti (Jira) sellaisenaan, sisällöltään yhtenevä 3 §:n ja 4 §:n kanssa:

> Yleiset-raportti osioon
> valitaan päivämäärä ja kotikunta samalta päivältä
>
> Opetuksen järjestäjän mukaan yhteenlaskettu oppilasmäärä oppilaan kotikunnan ja iän mukaan
>
> oppilaskohtaiset sarakkeet:
> Oppija oid, oppijan master oid, hetu, opiskeluoikeuden oid, yksilöity, etunimet, sukunimi,
> kotikunta, suorituksen tyyppi, aikaleima, oppilaitos, vuosiluokka, lukuvuoden alkamispäivä
> (esiopetukselle opiskeluoikeuden alkamispäivä), ikäryhmä ? (6, 7-12, 13-15 jne)

Huom: tiketti itsessään ei mainitse erityisen tuen sarakkeita, 20 HTP:tä, pidennettyä
oppivelvollisuutta eikä hetuttomia oppijoita — nämä nousivat esiin vasta kokouksessa (ks. 5 §, 6 §).

## 3. Kokouksessa päätetty rakenne

- **Yhden päivän snapshot** — ei aikaväliä. Valitaan päivämäärä + kotikunta (ratkaistaan samalta
  päivältä).
- Ydin-aggregaattinäkymä: **oppilasmäärä opetuksen järjestäjän × kotikunnan × iän mukaan**.
- Vähintään **kaksi välilehteä**:
  1. Aggregaattivälilehti (yhteenlaskettu oppilasmäärä em. ryhmittelyillä)
  2. **"Oppijat"-välilehti** (päätetty myöhemmässä kokouksessa, ks. 4 §:n korvattu sisältö) —
     rivi per oppija, sarakkeina oppijanumero + tosi/epätosi-liput jokaiselle
     aggregaattivälilehden ikäryhmäsarakkeelle, jäljitettävyyttä varten.
- Välilehtien tarkka asettelu / datan esitystapa on vielä auki, vaatii lisäsuunnittelua.
- **Data-only** — ei euromääräistä VOS-laskentakaavaa Koskessa. "Vossin muutokset" (varsinainen
  rahoituskaavan laskenta) käsitellään myöhemmin, erikseen.

## 4. Oppijakohtaiset sarakkeet

**KORVATTU** myöhemmässä kokouksessa (ks. 10.1 §): alkuperäinen, laajempi "Varmat"-sarakelista
(oppija oid, hetu, etunimet, sukunimi, kotikunta, oppilaitos, jne.) ei ole enää yksilöidyn
välilehden suunnitelma. Uusi, päätetty "Oppijat"-välilehti sisältää vain oppijanumeron ja
tosi/epätosi-liput aggregaattivälilehden ikäryhmäsarakkeille — ks. 10.1 §. Alkuperäinen lista on
jätetty tähän historiallisena kontekstina, koska osa 5 §:n avoimista kysymyksistä viittaa siihen.

### Varmat (historiallinen, korvattu — ks. yllä)

- Oppija oid
- Oppijan master oid
- Hetu
- Opiskeluoikeuden oid
- Yksilöity
- Etunimet
- Sukunimi
- Kotikunta
- Suorituksen tyyppi
- Aikaleima
- Oppilaitos
- Vuosiluokka
- Lukuvuoden alkamispäivä (esiopetukselle: opiskeluoikeuden alkamispäivä)

### Ikäryhmä — ratkennut aggregaattitasolla, yksilöidyllä tasolla vielä auki

Saaduissa esimerkkikyselyissä (8 §) bucket-rajat ovat: 6, 7–12, 13–15, 16 (jaettuna pidennetyn
oppivelvollisuuden mukaan), yhteensä (6–16). Ks. 5 §, kohta 5 avoimesta kysymyksestä siitä, pitääkö
16-vuotiaiden alaryhmä näkyä myös yksilöidyllä välilehdellä jäljitettävyyden vuoksi. **Päivitys
10.1 §:ssä: kokous päätti Oppijat-välilehden, joka näyttää juuri tämän — ratkaisee kysymyksen
näyttämällä sen, mutta jättää 5 §:n kohdan 5 (erityisen tuen tiedon yksilöllisyys) ratkaisematta —
kokous päätti näyttää sen tietoisena ristiriidasta, ei ratkaissut sitä, ks. 10.1 §.**

### Ei sisällytetä (yliviivattu muistiinpanoissa — päätetty jättää pois toistaiseksi)

- Vamma, sairaus tai rajoitteen peruste
- Toiminta-alue
- Varhennettu oppivelvollisuus
- Tuen päätöksen jakso
- Tavoitekokonaisuuksittain
- Lähdejärjestelmä
- Lähdejärjestelmän tunniste

> Näiden poisjättö pienentää raportin tietosuojaherkkyyttä — erityisen tuen tietoa ei tässä
> vaiheessa yhdistetä yksilöitävissä olevaan oppijaan. Tietosuoja-arviointi ei ole tämän vuoksi
> tällä hetkellä pullonkaula; tulee ajankohtaiseksi vain jos joku ehdottaa näiden lisäämistä
> myöhemmin.

## 5. Tiedossa olevat ongelmat

1. **Pidennetty oppivelvollisuus** -logiikka on muuttunut. Ari korjaa asiaa liittyvän tiketin.
   Saaduissa esimerkkikyselyissä (ks. 8 §) ikäryhmän 16 jako pidennettyyn oppivelvollisuuteen on
   tehty kahdella eri tavalla kahdessa eri kyselyssä — toisessa suoraan
   `d.pidennetty_oppivelvollisuus`-kentällä, toisessa johdettuna kentistä
   `opetus_vamman_sairauden_tai_rajoitteen_perusteella` / `toiminta_alueittain_opiskelu`. Nämä
   eivät välttämättä tarkoita samaa asiaa — pitää selvittää kumpi on oikea/kanoninen ennen kuin
   logiikka päätyy raporttiin.
2. **Hetuttomat oppijat**: saatu esimerkkikysely (8 §) käsittelee heidät omana erillisenä
   tarkasteluna (`WHERE he.hetu IS NULL`), ei pudota heitä pois. Koska
   `r_kotikuntahistoria` liittyy `master_oid`:n kautta (ei hetun), hetuttomilla oppijoilla *voi*
   silti olla ratkeava kotikunta joissain tapauksissa — kysymys ei siis ole "ei koskaan kotikuntaa"
   vaan "tarvitsee oman erittelyn sen suhteen, resolvautuuko kotikunta vai ei". Päätös siitä,
   näytetäänkö hetuttomat omana rivinä/välilehtenä vai `'hetuton'`-fallback-ryhmänä pääraportissa,
   on vielä auki.
3. Välilehtien layout / datan esitystapa vaatii vielä suunnittelua.
4. ~~Ikäryhmä-bucketit eivät vielä lukkoon lyötyjä~~ — ratkennut, ks. 8 §: 6, 7–12, 13–15, 16
   (jaettuna pidennetyn oppivelvollisuuden mukaan, ks. kohta 1 yllä), yhteensä (6–16).
5. **Uusi kysymys**: ikäryhmän 16 jako pidennetyn oppivelvollisuuden mukaan on aggregaattitasolla
   pelkkä COUNT-jako, mutta yksilöity välilehti vaatii jäljitettävyyden aggregaattilukuihin (3 §).
   16-vuotiaalle oppijalle tämä tarkoittaisi käytännössä sen näyttämistä, kumpaan alaryhmään hän
   kuuluu — mikä on juuri sitä erityisen tuen tason yksilöityä tietoa, joka 4 §:ssä päätettiin
   jättää pois oppijakohtaisista sarakkeista. Tätä ristiriitaa ei ole vielä ratkaistu.
6. **KORJATTU, turvallisuuskriittinen**: kaikki kolme 8 §:n esimerkkikyselyä käyttävät
   `koski_confidential.r_kotikuntahistoria`-taulua (confidential-skeema), joka sisältää MYÖS
   turvakielto-oppijoiden rivit (`RKotikuntahistoriaTable.turvakielto: Boolean`,
   `RaportointiDatabaseSchema.scala:598`). Julkinen `r_kotikuntahistoria`-taulu (ilman
   skeemaetuliitettä) on ladattu erikseen suodattaen turvakielto-rivit pois
   (`RaportointiDatabase.scala:443`: `RKotikuntahistoria ++= historia.filterNot(_.turvakielto)`),
   kun taas `koski_confidential`-varianttiin ladataan kaikki rivit turvakiellosta riippumatta
   (`HenkiloLoader.scala:47`). Confidential-varianttia käyttämällä raportti paljastaisi
   turvakiellon alaisten oppijoiden todellisen kotikunnan hetu/nimi-tason tiedon rinnalla — juuri
   sitä mitä kyseinen skeemajako on olemassa estämässä. Olemassa oleva
   `showKotikuntaPvmInput`-toteutus (`PerusopetuksenRaportitRepository.scala`) käyttää oikein
   julkista taulua. **Kotikuntaraportin kysely tulee kirjoittaa julkista
   `r_kotikuntahistoria`-taulua vasten, ei confidential-varianttia** — turvakiellon alaiset
   oppijat eivät tällöin resolvoi kotikuntaa lainkaan ja putoavat samaan "ei resolvoitunutta
   kotikuntaa" -koriin kuin hetuttomat (2 §), mikä on turvallinen lopputulos.

   **Toteutunut ratkaisu (`Kotikuntalaskelma.scala`, aggregaattikysely):** kaksitasoinen
   fallback-ketju, joka estää turvakiellon vuotamisen kahdesta eri suunnasta:
   1. Ensisijainen lähde on julkinen `r_kotikuntahistoria` — turvakiellon alaiset rivit eivät
      koskaan päädy tähän tauluun (ks. yllä), joten `kkh`-liitos ei koskaan osu heille.
   2. Varalähteenä käytetään `r_henkilo.kotikunta`/`kotikunta_nimi_fi` (nykyinen, ei
      historiallinen kotikunta), mutta **vain jos `not he.turvakielto`**:
      `case when he.turvakielto then null else he.kotikunta end`. `r_henkilo`-taulu EI ole
      turvakielto-suodatettu, joten ilman tätä eksplisiittistä tarkistusta varalähde vuotaisi
      turvakiellon alaisten oppijoiden osoitetiedon toista kautta.
   3. Jos molemmat lähteet päätyvät NULL:iin (turvakielto AINA, tai hetuton/muu ei-resolvoituva
      tapaus), nimisarake coalescetaan lopulta kirjaimelliseen `'Ei tiedossa'`-arvoon;
      koodisarake jää NULL:ksi. Oppija lasketaan silti oikein mukaan ikäryhmä-/
      järjestäjätotaaleihin — vain kotikunta-tieto piilotetaan.

   **Ei-toivottu mutta hyväksyttävissä oleva sivuvaikutus, ei erikseen päätetty**: "Ei tiedossa"
   -koriin päätyvät turvakielto, hetuttomat JA muut ilman muuta syytä resolvoitumattomat oppijat
   samaan koriin erottamattomina — raportista ei voi päätellä, mistä syystä yksittäinen
   oppijanumero on tässä korissa. Tämä on lievä yksityisyyshyöty (korin syytä ei voi arvata), mutta
   ei ole muotoiltu tietoiseksi vaatimukseksi missään vaiheessa — kannattaa varmistaa että tämä on
   hyväksyttävä lopputulos ennen tuotantoon vientiä. "Oppijat"-välilehti (10.1 §) ei koske
   kotikuntaa lainkaan, joten sillä ei ole vastaavaa turvakielto-riskiä.

## 6. Reunaehdot

- **20 HTP** (henkilötyöpäivää) varattu koko toteutukseen.
- Koko hanke on **"alustava speksi, jos saadaan VM:ltä rahaa"** — riippuvainen Valtiovarain-
  ministeriön rahoituksesta.

## 7. Seuraavat askeleet

- [x] Päätä otetaanko erityisen tuen -sarakkeet mukaan — **päätetty jättää pois toistaiseksi**,
      ei siis vaadi tietosuoja-arviointia tässä vaiheessa (ks. kuitenkin 5 §, kohta 5 — uusi
      ristiriita 16-vuotiaiden yksilöidyn välilehden jäljitettävyyden kanssa).
- [x] Lyö lukkoon ikäryhmä-bucketit — saatu esimerkkikyselyistä (8 §): 6, 7–12, 13–15, 16
      (jaettuna), yhteensä.
- [ ] Selvitä kumpi pidennetyn oppivelvollisuuden kenttä on kanoninen:
      `d.pidennetty_oppivelvollisuus` vai johdettu `toiminta_alueittain_opiskelu` /
      `opetus_vamman_sairauden_tai_rajoitteen_perusteella` (ks. 8.4 §).
- [ ] Päätä hetuttomien oppijoiden esitystapa: oma välilehti/rivi (8.2 §) vai `'hetuton'`-
      fallback-ryhmä pääraportissa (8.1 §) — ja varmista ettei 8.2 §:n päivämäärärajaamaton
      `kkh`-liitos tuota tuplalaskentaa.
- [ ] Varmista 8.3 §:n `WHERE`-lausekkeeseen siirretyn kotikuntahistoria-aikarajauksen
      tarkoituksenmukaisuus (LEFT JOIN muuttuu käytännössä INNER JOIN:ksi).
- [ ] Ratkaise 16-vuotiaiden yksilöidyn välilehden jäljitettävyys-vs-erityisen tuen tieto
      -ristiriita (5 §, kohta 5).
- [ ] Suunnittele välilehtien tarkka layout / data-esitys.
- [ ] Odota Arin korjaus pidennetyn oppivelvollisuuden logiikkaan ja arvioi vaikutus raporttiin.
- [ ] Sovita 8 §:n kyselyt Koskin `RaportitService`/`Raportti`-traitiin ja
      `RaportitAccessResolver`-oikeustarkistuksiin; kirjoita vastaava kysely yksilöidylle
      välilehdelle (4 §:n sarakkeet, ei GROUP BY).
- [ ] Toteuta uusi `RaportinTyyppi`-case-object ja liitä se koulutusmuotoihin perusopetus,
      esiopetus, internationalschool, europeanschoolofhelsinki
      (`RaportitAccessResolver.raportinTyypitKoulutusmuodolle`) — korvaa `visibleForAllOrgs`-
      shimmin (ks. 9 §:n `TODO(TOR-2650)`-kommentti `web/app/raportit/Raportit.jsx`:ssä).

## 8. Tekninen toteutus

Raportti lukisi raportointikannasta (ei live-Koski-transaktiodatasta), samaan tapaan kuin muut
raportit (`RaportitService`/`Raportti`-trait). Alla kolme luonnoskyselyä, jotka on saatu
analyytikolta/asiantuntijalta suoraan raportointikantaa vasten ajettuina — nämä siis käyttävät
todellisia taulu-/sarakenimiä, toisin kuin aiempi arvaukseen perustuva luonnos. Kyselyt eivät ole
vielä täysin yhdenmukaisia keskenään (ks. 8.4 § huomiot) eikä niitä ole vielä sovitettu Koskin
Scala/Slick-raportointikerrokseen tai `RaportitAccessResolver`-oikeustarkistuksiin.

### 8.1 Aggregaattikysely — kaikki oppijat, kotikunta coalesced 'hetuton'-ryhmään

```sql
SELECT DISTINCT
    org.yritysmuoto,
    e.y_tunnus AS Ytunnus,
    a.koulutustoimija_nimi AS opetuksen_järjestäjä,
    e.kotipaikka AS opetuksen_järjestäjän_kuntakoodi,

    kkh.kotikunta AS kotikunnan_koodi,
    COALESCE(kkh.kotikunta_nimi_fi, 'hetuton') AS oppilaan_kotikunta,

    COUNT(DISTINCT CASE
        WHEN he.syntymaaika BETWEEN '2019-01-01' AND '2019-12-31'
        THEN he.master_oid
    END) AS kuusi,

    COUNT(DISTINCT CASE
        WHEN he.syntymaaika BETWEEN '2013-01-01' AND '2018-12-31'
        THEN he.master_oid
    END) AS seitsemän_kaksitoista,

    COUNT(DISTINCT CASE
        WHEN he.syntymaaika BETWEEN '2010-01-01' AND '2012-12-31'
        THEN he.master_oid
    END) AS kolmetoista_viisitoista,

    COUNT(DISTINCT CASE
        WHEN (
            he.syntymaaika BETWEEN '2009-01-01' AND '2009-12-31'
            AND (
                (d.toiminta_alueittain_opiskelu = 'true'
                 OR d.opetus_vamman_sairauden_tai_rajoitteen_perusteella = 'true')
                AND d.alku <= '2025-12-15'
                AND d.loppu >= '2025-12-15'
            )
        )
        THEN he.master_oid
    END) AS kuusitoista_opetus_vamman_sairauden_tai_rajoitteen_perusteella,

    COUNT(DISTINCT CASE
        WHEN (
            he.syntymaaika BETWEEN '2009-01-01' AND '2009-12-31'
            AND (
                (d.toiminta_alueittain_opiskelu = 'false'
                 OR d.opetus_vamman_sairauden_tai_rajoitteen_perusteella = 'false')
                AND d.alku <= '2025-12-15'
                AND d.loppu >= '2025-12-15'
            )
        )
        THEN he.master_oid
    END) AS kuusitoista_ei_opetus_vamman_sairauden_tai_rajoitteen_perusteella,

    COUNT(DISTINCT CASE
        WHEN he.syntymaaika BETWEEN '2009-01-01' AND '2019-12-31'
        THEN he.master_oid
    END) AS yhteensä

FROM koski.r_henkilo AS he
JOIN koski.r_opiskeluoikeus AS a ON he.oppija_oid = a.oppija_oid
JOIN koski.r_paatason_suoritus AS b ON a.opiskeluoikeus_oid = b.opiskeluoikeus_oid
LEFT JOIN koski.r_opiskeluoikeus_aikajakso AS d ON a.opiskeluoikeus_oid = d.opiskeluoikeus_oid
LEFT JOIN koski.esiopetus_opiskeluoik_aikajakso AS dd ON a.opiskeluoikeus_oid = dd.opiskeluoikeus_oid
JOIN koski.r_organisaatio AS e ON a.koulutustoimija_oid = e.organisaatio_oid
LEFT JOIN koski_confidential.r_kotikuntahistoria AS kkh
    ON kkh.master_oid = he.master_oid
    AND kkh.muutto_pvm <= '2025-12-15'
    AND (kkh.poismuutto_pvm >= '2025-12-15' OR kkh.poismuutto_pvm IS NULL)
JOIN organisaatio.organisaatio AS org ON org.organisaatio_oid = e.organisaatio_oid

WHERE
(
    (a.koulutusmuoto IN ('perusopetus', 'esiopetus')
     AND b.suorituksen_tyyppi IN ('perusopetuksenvuosiluokka', 'perusopetuksenoppimaara', 'esiopetuksensuoritus'))
    OR
    (a.koulutusmuoto = 'internationalschool'
     AND b.koulutusmoduuli_koodiarvo IN ('explorer','1','2','3','4','5','6','7','8','9')
     AND b.alkamispaiva BETWEEN '2025-08-01' AND '2025-12-15')
    OR
    (a.koulutusmuoto = 'europeanschoolofhelsinki'
     AND b.koulutusmoduuli_koodiarvo IN ('N1','N2','P1','P2','P3','P4','P5','S1','S2','S3','S4')
     AND b.alkamispaiva BETWEEN '2025-08-01' AND '2025-12-15')
)
AND
(
    (d.alku <= '2025-12-15' AND d.loppu >= '2025-12-15'
     AND d.tila IN ('lasna', 'eronnut', 'valmistunut')
     AND d.kotiopetus = 'false')
    OR
    (dd.alku <= '2025-12-15' AND dd.loppu >= '2025-12-15'
     AND dd.tila IN ('lasna', 'eronnut', 'valmistunut'))
)
AND he.syntymaaika BETWEEN '2009-01-01' AND '2019-12-31'

GROUP BY
    org.yritysmuoto, e.y_tunnus, a.koulutustoimija_nimi, e.kotipaikka,
    kkh.kotikunta, COALESCE(kkh.kotikunta_nimi_fi, 'hetuton')

ORDER BY a.koulutustoimija_nimi, kkh.kotikunta;
```

### 8.2 Hetuttomien oppijoiden erillistarkastelu

Sama perusrakenne, mutta rajattu `he.hetu IS NULL` -oppijoihin, ja ikäryhmän 16 jako tehdään
suoraan `d.pidennetty_oppivelvollisuus`-kentällä (ei 8.1 §:n johdetulla logiikalla — ks. 8.4 §).

```sql
SELECT
    org.yritysmuoto,
    e.y_tunnus AS Ytunnus,
    a.koulutustoimija_nimi AS opetuksen_järjestäjä,
    e.kotipaikka AS opetuksen_järjestäjän_kuntakoodi,

    kkh.kotikunta AS kotikunnan_koodi,
    kkh.kotikunta_nimi_fi AS oppilaan_kotikunta,

    COUNT(DISTINCT CASE WHEN he.syntymaaika BETWEEN '2019-01-01' AND '2019-12-31'
        THEN he.master_oid END) AS kuusi,
    COUNT(DISTINCT CASE WHEN he.syntymaaika BETWEEN '2013-01-01' AND '2018-12-31'
        THEN he.master_oid END) AS seitsemän_kaksitoista,
    COUNT(DISTINCT CASE WHEN he.syntymaaika BETWEEN '2010-01-01' AND '2012-12-31'
        THEN he.master_oid END) AS kolmetoista_viisitoista,
    COUNT(DISTINCT CASE
        WHEN he.syntymaaika BETWEEN '2009-01-01' AND '2009-12-31'
         AND d.pidennetty_oppivelvollisuus = 'true'
         AND d.alku <= '2025-12-15' AND d.loppu >= '2025-12-15'
        THEN he.master_oid END) AS kuusitoista_pidennetty,
    COUNT(DISTINCT CASE
        WHEN he.syntymaaika BETWEEN '2009-01-01' AND '2009-12-31'
         AND d.pidennetty_oppivelvollisuus = 'false'
         AND d.alku <= '2025-12-15' AND d.loppu >= '2025-12-15'
        THEN he.master_oid END) AS kuusitoista_EIpidennetty,
    COUNT(DISTINCT CASE WHEN he.syntymaaika BETWEEN '2009-01-01' AND '2019-12-31'
        THEN he.master_oid END) AS yhteensä

FROM koski.r_henkilo AS he
JOIN koski.r_opiskeluoikeus AS a ON he.oppija_oid = a.oppija_oid
JOIN koski.r_paatason_suoritus AS b ON a.opiskeluoikeus_oid = b.opiskeluoikeus_oid
LEFT JOIN koski.r_opiskeluoikeus_aikajakso AS d ON a.opiskeluoikeus_oid = d.opiskeluoikeus_oid
LEFT JOIN koski.esiopetus_opiskeluoik_aikajakso AS dd ON a.opiskeluoikeus_oid = dd.opiskeluoikeus_oid
JOIN koski.r_organisaatio AS e ON a.koulutustoimija_oid = e.organisaatio_oid
LEFT JOIN koski_confidential.r_kotikuntahistoria AS kkh ON kkh.master_oid = he.master_oid
JOIN organisaatio.organisaatio AS org ON org.organisaatio_oid = e.organisaatio_oid

WHERE
(
    (a.koulutusmuoto IN ('perusopetus', 'esiopetus')
     AND b.suorituksen_tyyppi IN ('perusopetuksenvuosiluokka', 'perusopetuksenoppimaara', 'esiopetuksensuoritus'))
    OR
    (a.koulutusmuoto = 'internationalschool'
     AND b.koulutusmoduuli_koodiarvo IN ('explorer','1','2','3','4','5','6','7','8','9')
     AND b.alkamispaiva BETWEEN '2025-08-01' AND '2025-12-15')
    OR
    (a.koulutusmuoto = 'europeanschoolofhelsinki'
     AND b.koulutusmoduuli_koodiarvo IN ('N1','N2','P1','P2','P3','P4','P5','S1','S2','S3','S4')
     AND b.alkamispaiva BETWEEN '2025-08-01' AND '2025-12-15')
)
AND
(
    (d.alku <= '2025-12-15' AND d.loppu >= '2025-12-15'
     AND d.tila IN ('lasna', 'eronnut', 'valmistunut')
     AND d.kotiopetus = 'false')
    OR
    (dd.alku <= '2025-12-15' AND dd.loppu >= '2025-12-15'
     AND dd.tila IN ('lasna', 'eronnut', 'valmistunut'))
)
AND he.hetu IS NULL
AND he.syntymaaika BETWEEN '2009-01-01' AND '2019-12-31'

GROUP BY
    org.yritysmuoto, e.y_tunnus, a.koulutustoimija_nimi, e.kotipaikka,
    kkh.kotikunta, kkh.kotikunta_nimi_fi

ORDER BY a.koulutustoimija_nimi;
```

### 8.3 Aggregaattikysely — vaihtoehto, ei coalesce-fallbackia

Sama kuin 8.1 §, mutta kotikunta ei coalesce'ta `'hetuton'`-arvoon, ikäryhmän 16 jako käyttää
`d.pidennetty_oppivelvollisuus`-kenttää (kuten 8.2 §) ja kotikuntahistorian aikarajaus on siirretty
`JOIN ... ON`-lausekkeesta `WHERE`-lausekkeeseen (ks. 8.4 § — tällä on merkitystä).

```sql
SELECT DISTINCT
    org.yritysmuoto,
    e.y_tunnus AS Ytunnus,
    a.koulutustoimija_nimi AS opetuksen_järjestäjä,
    e.kotipaikka AS opetuksen_järjestäjän_kuntakoodi,

    kkh.kotikunta AS kotikunnan_koodi,
    kkh.kotikunta_nimi_fi AS oppilaan_kotikunta,

    COUNT(DISTINCT CASE WHEN he.syntymaaika BETWEEN '2019-01-01' AND '2019-12-31'
        THEN he.master_oid END) AS kuusi,
    COUNT(DISTINCT CASE WHEN he.syntymaaika BETWEEN '2013-01-01' AND '2018-12-31'
        THEN he.master_oid END) AS seitsemän_kaksitoista,
    COUNT(DISTINCT CASE WHEN he.syntymaaika BETWEEN '2010-01-01' AND '2012-12-31'
        THEN he.master_oid END) AS kolmetoista_viisitoista,
    COUNT(DISTINCT CASE
        WHEN he.syntymaaika BETWEEN '2009-01-01' AND '2009-12-31'
         AND d.pidennetty_oppivelvollisuus = 'true'
         AND d.alku <= '2025-12-15' AND d.loppu >= '2025-12-15'
        THEN he.master_oid END) AS kuusitoista_pidennetty,
    COUNT(DISTINCT CASE
        WHEN he.syntymaaika BETWEEN '2009-01-01' AND '2009-12-31'
         AND d.pidennetty_oppivelvollisuus = 'false'
         AND d.alku <= '2025-12-15' AND d.loppu >= '2025-12-15'
        THEN he.master_oid END) AS kuusitoista_EIpidennetty,
    COUNT(DISTINCT CASE WHEN he.syntymaaika BETWEEN '2009-01-01' AND '2019-12-31'
        THEN he.master_oid END) AS yhteensä

FROM koski.r_henkilo AS he
JOIN koski.r_opiskeluoikeus AS a ON he.oppija_oid = a.oppija_oid
JOIN koski.r_paatason_suoritus AS b ON a.opiskeluoikeus_oid = b.opiskeluoikeus_oid
LEFT JOIN koski.r_opiskeluoikeus_aikajakso AS d ON a.opiskeluoikeus_oid = d.opiskeluoikeus_oid
LEFT JOIN koski.esiopetus_opiskeluoik_aikajakso AS dd ON a.opiskeluoikeus_oid = dd.opiskeluoikeus_oid
JOIN koski.r_organisaatio AS e ON a.koulutustoimija_oid = e.organisaatio_oid
LEFT JOIN koski_confidential.r_kotikuntahistoria AS kkh ON kkh.master_oid = he.master_oid
JOIN organisaatio.organisaatio AS org ON org.organisaatio_oid = e.organisaatio_oid

WHERE
(
    (a.koulutusmuoto IN ('perusopetus', 'esiopetus')
     AND b.suorituksen_tyyppi IN ('perusopetuksenvuosiluokka', 'perusopetuksenoppimaara', 'esiopetuksensuoritus'))
    OR
    (a.koulutusmuoto = 'internationalschool'
     AND b.koulutusmoduuli_koodiarvo IN ('explorer','1','2','3','4','5','6','7','8','9')
     AND b.alkamispaiva BETWEEN '2025-08-01' AND '2025-12-15')
    OR
    (a.koulutusmuoto = 'europeanschoolofhelsinki'
     AND b.koulutusmoduuli_koodiarvo IN ('N1','N2','P1','P2','P3','P4','P5','S1','S2','S3','S4')
     AND b.alkamispaiva BETWEEN '2025-08-01' AND '2025-12-15')
)
AND
(
    (d.alku <= '2025-12-15' AND d.loppu >= '2025-12-15'
     AND d.tila IN ('lasna', 'eronnut', 'valmistunut')
     AND d.kotiopetus = 'false')
    OR
    (dd.alku <= '2025-12-15' AND dd.loppu >= '2025-12-15'
     AND dd.tila IN ('lasna', 'eronnut', 'valmistunut'))
)
AND (kkh.muutto_pvm <= '2025-12-15' AND (kkh.poismuutto_pvm >= '2025-12-15' OR kkh.poismuutto_pvm IS NULL))
AND he.syntymaaika BETWEEN '2009-01-01' AND '2019-12-31'

GROUP BY
    org.yritysmuoto, e.y_tunnus, a.koulutustoimija_nimi, e.kotipaikka,
    kkh.kotikunta, kkh.kotikunta_nimi_fi

ORDER BY a.koulutustoimija_nimi, kkh.kotikunta;
```

### 8.4 Kyselyjen väliset ristiriidat — tarkistettava ennen kuin näistä valitaan yksi

- **Turvallisuuskriittinen, ks. 5 §:n kohta 6**: kaikki kolme kyselyä käyttävät väärää
  kotikuntahistoria-taulua (`koski_confidential.r_kotikuntahistoria` sisältää turvakielto-
  oppijoiden rivit). Toteutus käyttää julkista `r_kotikuntahistoria`-taulua tämän vuoksi.
- **Pidennetty oppivelvollisuus, ikäryhmä 16**: 8.1 § johtaa jaon kentistä
  `toiminta_alueittain_opiskelu` / `opetus_vamman_sairauden_tai_rajoitteen_perusteella`, kun taas
  8.2 § ja 8.3 § käyttävät suoraan `d.pidennetty_oppivelvollisuus`-kenttää. Nämä eivät ole
  taatusti sama asia — kumpi on kanoninen, pitää selvittää (ks. myös 5 §, kohta 1).
- **8.2 §:n `kkh`-liitos ei rajaa aikaväliä** — LEFT JOIN kotikuntahistoriaan ilman
  `muutto_pvm`/`poismuutto_pvm`-ehtoa voi tuottaa useamman historiarivin per `master_oid`, mikä
  vääristäisi COUNT DISTINCT -lukuja jos/kun ehto puuttuu tarkoituksella tai vahingossa.
- **8.3 §:ssä kotikuntahistorian aikarajaus on `WHERE`-lausekkeessa, ei `JOIN ... ON`:issa.**
  Koska `kkh` on `LEFT JOIN`, tämä muuttaa sen käytännössä `INNER JOIN`:ksi — oppijat joilla ei
  ole aikavälille osuvaa kotikuntahistoriariviä (esim. osa hetuttomista) pudotetaan kokonaan pois
  tuloksesta sen sijaan että näkyisivät NULL-kotikunnalla. Tämä voi olla tarkoituksellista (8.3 §
  ehkä ajateltu 8.2 §:n täydentäväksi "kotikunta löytyi" -näkymäksi), mutta pitää varmistaa
  kyselyn kirjoittajalta.
- Kyselyt on ajettu esimerkkipäivälle 2025-12-15 — tuotannossa päivämäärä olisi raportin
  parametri (`:paiva`), ei kovakoodattu literaali.
- Ei vielä sovitettu Koskin `RaportitService`/`Raportti`-traitiin, `RaportitAccessResolver`-
  oikeustarkistuksiin, eikä yksilöidyn välilehden (3 §) kyselyä ole vielä kirjoitettu — nämä
  aggregaattikyselyt eivät sellaisenaan tuota 4 §:n oppijakohtaisia sarakkeita riveittäin.
- Ennen toteutusta kannattaa myös katsoa muiden VOS-raporttien (esim.
  `AikuistenPerusopetuksenOppijamäärätRaportti`) todellinen Slick-kysely mallipohjaksi siitä,
  miten raportointikantaa käytetään Koskin sisällä idiomaattisesti.

## 9. Pääsyoikeusmalli ja frontend: raportin avausnäkymä (aloitettu)

### 9.1 Pääsyoikeusmalli — korjattu ymmärrys

Ensimmäinen luonnos oletti raportin olevan "organisaatioton" (täysin valtakunnallinen, ei
minkään organisaation skoopittama), mikä osoittautui vääräksi lähtökohdaksi. "Opetuksen
järjestäjä" (koulutustoimija) ON organisaatio Koskin mallissa — 8 §:n kyselyt liittyvät
`r_organisaatio`/`organisaatio.organisaatio`-tauluihin aivan tavalliseen tapaan, ja
`RaportitAccessResolver` kohtelee `Koulutustoimija`a organisaatiopuun solmutyyppinä
(`organisaatioHierarkia.toOrganisaatio.isInstanceOf[Koulutustoimija]`).

Todellinen syy harkita uudelleen: **useimmilla raportin käyttäjillä ei todennäköisesti ole
`hasGlobalReadAccess`-oikeutta** — he ovat oletettavasti yksittäisten oppilaitosten/koulutus-
toimijoiden virkailijoita, eivät OPH:n pääkäyttäjiä. Ja myös ne, joilla globaali oikeus on,
saattavat silti haluta rajata näkymän yhteen organisaatioon kerrallaan ison valtakunnallisen
taulukon sijaan. Tämä tarkoittaa, että Kotikuntalaskelma on rakenteeltaan **täsmälleen sama
kuin muut VOS-oppijamäärä-raportit** (esim. `AikuistenPerusopetuksenOppijamäärätRaportti`):

- Organisaatiovalitsin (`OrganisaatioDropdown`) valitsee `oppilaitosOid`/`organisaatioOid`:n,
  joka laajennetaan `RaportitAccessResolver.kyselyOiditOrganisaatiolle`:lla alipuuksi.
- SQL-kyselyn WHERE-lausekkeessa käytetään samaa kaavaa kuin muissakin raporteissa:
  `#${if (u.hasGlobalReadAccess) "true" else "false"} or organisaatio_oid = any($käyttäjänOrganisaatioOidit)`
  — eli `hasGlobalReadAccess`-oikeudella näkee kaiken, muuten vain valitun organisaation
  alipuun. Tämä on olemassa oleva, valmiiksi määritelty `Session`-ominaisuus
  (`hasGlobalReadAccess = globalAccess.contains(AccessType.read)`, `Session.scala:160`), ei
  mitään uutta.
- `raportit.rajatut`-lista (`RaportitAccessResolver.checkRaporttiAccessIfAccessIsLimited`) voi
  edelleen kaventaa NÄKYVYYTTÄ tietyille käyttäjä-OIDeille, mutta se on aina lisäsuodin jo
  muuten organisaationsa/koulutusmuotonsa kautta näkyvän raportin päällä — se ei itsessään
  myönnä pääsyä ilman organisaatio-/koulutusmuoto-oikeutta. Oikea tapa tehdä raportti
  ylipäätään näkyväksi on lisätä sille oma `RaportinTyyppi`-case-object
  (`Raportti.scala:121`) ja liittää se `RaportitAccessResolver.raportinTyypitKoulutusmuodolle`
  -funktiossa niihin koulutusmuotoihin joita 8 §:n kyselyt kattavat: `perusopetus`,
  `esiopetus`, `internationalschool`, `europeanschoolofhelsinki` — sama malli kuin
  `LukioDiaIbInternationalESHOpiskelijamaarat`, joka on liitetty viiteen eri
  koulutusmuoto-caseen samalla tavalla.

### 9.2 Frontend: raportin avausnäkymä

Raportin avausnäkymä (organisaatiovalitsin + päivämäärän valinta + "Lataa Excel-tiedosto"
-painike) käyttää suoraan olemassa olevaa `RaporttiPaivalta.jsx`-komponenttia — samaa jota
kaikki muutkin yhden päivän VOS-raportit (esiopetus-vos, perusopetus-vos, tuva-perusopetus-vos,
`AikuistenPerusopetuksenOppijamäärätRaportti`) käyttävät. Ensimmäisessä versiossa rakennettiin
tätä varten oma `KotikuntalaskelmaPaivalta.jsx`-komponentti ilman organisaatiovalitsinta — se on
nyt poistettu 9.1 §:n korjauksen myötä, koska organisaatiovalitsin tarvitaan kuten muillakin
raporteilla. Ei erillistä `kotikuntaPvm`-kenttää — kotikunta ratkaistaan samalta päivältä kuin
muu data (3 §), joten `showKotikuntaPvmInput`-propsia ei käytetä.

Kytketty "Yleiset"-kategoriaan (`web/app/raportit/Raportit.jsx`, kategoria `muut` — sen
välilehden otsikko on jo valmiiksi "Yleiset") Päällekkäiset opiskeluoikeudet -raportin rinnalle.

**Väliaikainen näkyvyys-shimmi, merkitty `TODO(TOR-2650)`-kommentilla
`web/app/raportit/Raportit.jsx`:ssä:** raportin näkyvyys on toistaiseksi toteutettu
`visibleForAllOrgs: true` -lipulla (sama mekanismi kuin Päällekkäiset opiskeluoikeudet
-raportilla), eli se näkyy kaikille joilla on jokin organisaatio-oikeus raportteihin
ylipäätään. Tämä on tarkoituksella väliaikainen — 9.1 §:n mukainen oikea ratkaisu (uusi
`RaportinTyyppi` + koulutusmuoto-kytkentä + tarvittaessa `raportit.rajatut`) korvaa tämän kun
backend-puoli toteutetaan. Aiempi, virheellinen TODO ("käytä `raportit.rajatut`-listaa suoraan
ilman organisaatio-/koulutusmuoto-kytkentää") ja siihen liittyvä huoli
`RaportitContent`-komponentin globaalista "ei organisaatiokäyttöoikeuksia" -virheestä eivät enää
päde, koska raportti EI ole organisaatioton — tavallinen organisaatio-oikeuksien kautta kulkeva
näkyvyyspolku toimii sille aivan kuten muillekin raporteille.

Ei vielä tehty: backend-endpoint (servlet-reitti, request-case-class, `RaportitService`-metodi),
Excel-kirjoitus, yksilöidyn välilehden kysely, uusi `RaportinTyyppi`-case-object eikä sen
koulutusmuoto-kytkentä.

## 10. Backend-endpointin toteutus ja testifixtuurit

### 10.1 Oppijat-välilehti (päätetty jatkokokouksessa, korvaa 4 §:n alkuperäisen sarakelistan)

Toinen välilehti ("Oppijat") näyttää yhden rivin per oppija: sarakkeina **oppijanumero** (tässä
`master_oid`, sama tunniste jolla aggregaattivälilehti laskee DISTINCT-oppilasmäärät) sekä
tosi/epätosi-liput viidelle aggregaattivälilehden ikäryhmäsarakkeelle (kuusi,
seitsemänKaksitoista, kolmetoistaViisitoista, kuusitoistaErityisenTuenPerusteella,
kuusitoistaEiErityisenTuenPerusteella) — samalla suodatuslogiikalla kuin aggregaattikysely,
ilman ryhmittelyä opetuksen järjestäjän tai kotikunnan mukaan. **Yhteensä-sarake jätetty pois
tästä välilehdestä**: koska kyselyn WHERE-ehto jo rajaa tulosjoukon 6–16-vuotiaisiin, jokainen
Oppijat-välilehdellä ylipäätään näkyvä rivi täyttää "yhteensä"-ehdon aina — sarake olisi
tautologia (aina tosi), ei informaatiota. Aggregaattivälilehdellä "yhteensä" on edelleen
mielekäs, koska se on oikea rivikohtainen summaluku (montako oppijaa juuri tässä
järjestäjä×kotikunta-ryhmässä), ei per-oppija-lippu.

**Huomio, ei ratkaistu, vain kirjattu (käyttäjän ohjeen mukaisesti toteutettu sellaisenaan):**
tämä paljastaa yksilötasolla, onko 16-vuotias oppija laskettu "erityisen tuen perusteella"
-sarakkeeseen — eli suoraan erityisen tuen statuksen tiettylle nimetylle
oppijanumerolle (vaikkakin vain kahden aggregaattisarakkeen kautta johdettuna, ei raakana
erityisen tuen tyyppitietona). Tämä on ristiriidassa 4 §:n "Ei sisällytetä"-päätöksen hengen
kanssa (erityisen tuen tietoa ei yhdistetä yksilöitävissä olevaan oppijaan). Käyttäjä on
tietoinen ristiriidasta ja käsittelee sitä myöhemmin erikseen; ei blokannut toteutusta.

Ei sisällä hetua, nimeä, kotikuntaa eikä oppilaitosta — pelkkä oppijanumero + liput. Tämä
tarkoittaa myös, ettei tästä välilehdestä voi jäljittää tarkasti MIHIN yksittäiseen
aggregaattivälilehden riviin (mikä opetuksen järjestäjä × mikä kotikunta) oppija on laskettu —
vain siihen MIHIN IKÄRYHMÄÄN. Tarkempi jäljitettävyys vaatisi järjestäjä-/kotikunta-sarakkeita,
joita ei pyydetty.

Aggregaattivälilehden backend on nyt toteutettu (`Kotikuntalaskelma.scala`, `RaportitService.
kotikuntalaskelma`, `RaportitServlet` `/kotikuntalaskelma`-reitti) 8.1 §:n kyselyn pohjalta,
korjattuna 5 §:n kohdan 6 mukaisesti (julkinen `r_kotikuntahistoria`, ei confidential-varianttia)
ja ikäryhmät parametrisoitu (`extract(year from :paiva)` kovakoodattujen vuosilukujen sijaan).
Manuaalisessa testauksessa löytyi ja korjattiin yksi SQL-bugi: `extract(year from $päivä)` on
Postgresille moniselitteinen bind-parametrin kanssa (`function pg_catalog.extract(unknown,
unknown) is not unique`) — korjattu eksplisiittisellä `::date`-castilla (`$päivä::date`).

**Testifixtuurit puuttuivat käytännössä kokonaan.** Suurin osa `KoskiSpecificMockOppijat.scala`:n
perusopetus-/esiopetusoppijoista käyttää vanhan muotoisia hetuja, jotka dekoodautuvat 1900-luvun
syntymävuosiksi — eli ne ovat raportointikannassa "aikuisia", eivät raportin 6-16-vuotiaiden
ikäikkunaan osuvia lapsia. Tämän vuoksi olemassa olevalla datalla raportti näytti käytännössä
yhden rivin (`esikoululainen2025`, esiopetus, aktiivinen 13.8.2025-31.8.2026).

Lisätty kuusi uutta testioppijaa `KoskiSpecificMockOppijat.scala`:n loppuun (ks. tiedoston
kommentti, OID:t eivät siirry koska lisäys on listan lopussa) ja niiden opiskeluoikeudet
`KoskiSpecificDatabaseFixtureCreator.defaultOpiskeluOikeudet`-listan loppuun, kaikki
Jyväskylän normaalikoulussa, avoimin (päättymätön) perusopetuksen 1. luokan läsnä-jaksoin
1.8.2022 alkaen, jotta ikä ratkeaa pelkästä syntymäajasta:

- `kotikuntalaskelmaKuusivuotias` (s. 2020, kotikunta Jyväskylä) — 6-vuotiaiden ryhmä
- `kotikuntalaskelmaSeitsemanKaksitoista` (s. 2017, Helsinki) — 7-12-vuotiaiden ryhmä
- `kotikuntalaskelmaKolmetoistaViisitoista` (s. 2012, Helsinki) — 13-15-vuotiaiden ryhmä
- `kotikuntalaskelmaKuusitoistaErityinen` (s. 2010, Jyväskylä) — 16 v., `toimintaAlueittainOpiskelu`
  asetettu → testaa "erityisen tuen perusteella" -alaryhmää
- `kotikuntalaskelmaKuusitoistaEiErityista` (s. 2010, Helsinki) — 16 v., ei erityisen tuen lippuja
- `kotikuntalaskelmaHetuton` (s. 2015, ei hetua, ei kotikuntaa) — testaa "Ei tiedossa"-ryhmää
  hetuttoman *lapsen* kautta (aiemmat hetuttomat fixturet olivat aikuisia)

**Ei katettu tällä fixtuurilisäyksellä**: turvakiellon alaista oppijaa (5 §:n kohta 6:n toinen
puoli — vaatisi oman `turvakielto = true` + kuntahistoria-fixtuurin), eikä `kotiopetus`-poisrajaus-
tapausta (olemassa olevat kotiopetus-fixturet ovat samasta syystä liian vanhoja/väärän ikäisiä
tälle raportille). Näiden lisääminen jää myöhemmäksi jos tarvitaan.

**Tunnettu sivuvaikutus, tietoisesti hyväksytty**: uusien aktiivisten oppijoiden lisääminen
Jyväskylän normaalikouluun — joka on erittäin paljon käytetty fixture-koulu muissa raporttitesteissä
(mm. oppijamäärä-raporttien testeissä) — todennäköisesti muuttaa niiden raporttien odotettuja
oppilasmääriä/snapshotteja tällä branchilla. Tietoinen päätös: fixturet pidetään branchilla
Kotikuntaraportin kehitystä varten, ja mahdollisesti rikkoutuvat testit korjataan myöhemmin erikseen.
**Riski**: kun testisviitta on jo osittain punainen tästä syystä, uusi, tästä muutoksesta
riippumaton regressio voi hukkua joukkoon huomaamatta. Mitigaatio: aja koko backend-testisarja
(`make backtest`) sekä ennen että heti fixtuurilisäyksen jälkeen ja vertaa diffiä — näin tiedetään
tarkalleen mitkä epäonnistumiset johtuvat juuri tästä muutoksesta, eikä myöhempi uusi regressio
pääse piiloutumaan jo-tiedettyjen joukkoon.

Fixturet päätettiin lopulta pitää pois committeista kokonaan (ks. keskustelu) — eivät siis mene
CI:hin eivätkä vaikuta jaettuun testisviittaan. Yllä oleva riski/mitigaatio-kappale koskisi vain
tilannetta jossa fixturet joskus committoidaan.

## 11. Riippumattomat tarkistuskierrokset

Kaksi erillistä agenttia tarkisti tämän session työn: yksi 10 §:n fixturet (compile-korrektius,
törmäykset, tuottavatko oikeat rivit raportille), toinen committoidun backend/frontend-koodin
(SQL-turvallisuus, confidential-taulun välttäminen, skeemanimien oikeellisuus, pääsyoikeusmalli,
lokalisointiavainten yhtenäisyys). Molemmat löysivät yhden todellisen, korjatun bugin:

- **Fixturet**: `KoskiSpecificDatabaseFixtureCreator.scala`:ssa `jyväskylänNormaalikoulu`
  (tuotu `MockOrganisaatiot`:sta) on String-OID, ei `Oppilaitos`-olio — `oppilaitos`/`toimipiste`-
  kentät vaativat jälkimmäisen. Korjattu kääräisemällä paikallisella `oppilaitos(oid): Oppilaitos`
  -apufunktiolla (tuotu `YleissivistavakoulutusExampleData`:sta) kaikissa 6 uudessa tuplassa
  (12 kohtaa). Sama tiedosto käyttää molempia `jyväskylänNormaalikoulu`-nimisiä identifiereitä
  kahdesta eri paikasta — sekaannuksen lähde.
- **Backend**: `Kotikuntalaskelma.scala`:n `r_kotikuntahistoria`-liitoksessa `muutto_pvm`
  (`Option[Date]`, voi olla NULL tuotannossa) puuttui NULL-turvallinen käsittely — `poismuutto_pvm`
  puolella oli jo `OR ... IS NULL`, mutta `muutto_pvm <= :paiva` puolella ei, jolloin oppija jolla
  on NULL `muutto_pvm` mutta muuten resolvoituva kotikunta olisi pudonnut virheellisesti
  "Ei tiedossa" -ryhmään. Korjattu `coalesce(kkh.muutto_pvm, '1900-01-01'::date) <= :paiva`
  -muotoon, samaa kaavaa kuin `EsiopetusRaportti.scala` käyttää samalle ongelmalle.

Muilta osin molemmat tarkistukset vahvistivat toteutuksen rakenteellisesti kunnossa olevaksi:
SQL-injektiosuojaus (kaikki arvot `$muuttuja`-bind-parametreina, ei `#$`-raakasplicejä),
julkinen-vs-confidential-taulu-valinta todennettu oikeaksi (ei vain kommentoitu sellaiseksi),
kaikki viitatut taulu-/sarakenimet olemassa, `DataSheet`-kenttäjärjestyssopimus pätee,
lokalisointiavaimet yhtenäiset molemmissa tiedostoissa, ja pääsyoikeusmallin/`visibleForAllOrgs`-
puutteet vastaavat täsmälleen aiemmin dokumentoitua (ei uusia yllätyksiä).

Kolmas, sovellusta ajettaessa (ei koodikatselmuksessa) löytynyt bugi, korjattu samana
sessiona: `kotikuntalaskelmaKuusitoistaErityinen`-fikstuurin `toimintaAlueittainOpiskelu`-jakso
alkoi `2022-08-01`, mikä rikkoo kaksi validaatiota `KoskiValidator.validatePäätösToimintaAlueittainOpiskelusta`-
metodissa: (1) jakson alkupäivä ei saa olla ennen konfiguraatioarvoa
`validaatiot.toimintaAlueittainJärjestettyVoimaan` (`reference.conf`, arvo `2026-08-01` —
kiinteä käyttöönottopäivä, ei suhteessa järjestelmän tämänhetkiseen päivään), ja (2) jakson
täytyy sisältyä samalle lisätiedolle asetettuun `tuenPäätöksenJaksot`-kenttään (uusi kenttä,
ei sama asia kuin vanhentunut `erityisenTuenPäätös`/`tehostetunTuenPäätös`), jota fikstuurissa
ei ollut lainkaan asetettu. Korjattu siirtämällä `toimintaAlueittainOpiskelu`-jakson alku
`2026-08-01`:een ja lisäämällä `tuenPäätöksenJaksot = Some(List(Tukijakso(Some(date(2026, 8, 1)), None)))`
samaan `PerusopetuksenOpiskeluoikeudenLisätiedot`-olioon, `PerusopetusExampleData.scala`:n
`päättötodistusOpiskeluoikeusUusillaLisätiedoilla`-esimerkin mukaisesti.

## 12. Muiden raportti-SQL:ien vertailu ja kotikuntahistorian parannus

Verrattiin `Kotikuntalaskelma.scala`:n kyselyä sisarraportteihin (`EsiopetusRaportti.scala`,
`PerusopetuksenOppijamäärätRaportti.scala`, `LukioDiaIbInternationalESHOpiskelijamaaratRaportti.scala`)
ilman muutoksia, sitten toteutettiin yksi näistä havainnoista:

- **Kotikuntahistorian aukkokäsittely (toteutettu).** Alkuperäinen kysely näytti "Ei tiedossa"
  aina kun `r_kotikuntahistoria`-liitos ei löytänyt kysytyn päivän kattavaa jaksoa — myös silloin
  kun oppijan nykyinen kotikunta (`r_henkilo.kotikunta`) oli täysin tiedossa, vain ei juuri sille
  historialliselle päivälle (esim. historiatieto alkaa myöhemmin kuin kysytty `päivä`, tai
  jaksoissa on aukko). `EsiopetusRaportti.scala` käyttää tässä tilanteessa `r_henkilo`:n
  nykyistä kotikuntaa varakotikuntana. Ennen vastaavan lisäämistä täällä varmistettiin
  eksplisiittisesti (uusi tarkistuskierros), ettei `r_henkilo.kotikunta`/`kotikunta_nimi_fi` ole
  suodatettu turvakiellon alaisille julkisessa skeemassa — se **ei ole** (`HenkiloLoader.scala`:n
  `buildRHenkilöRow` ei sisällä turvakielto-haaraa, toisin kuin `RaportointiDatabase.loadKotikuntahistoria`,
  joka suodattaa `filterNot(_.turvakielto)`). Suora varakotikunnan käyttö olisi siis vuotanut
  turvakiellon alaisten oppijoiden osoitetiedon takaisin — täsmälleen sama riski kuin 5 §:ssä
  alun perin vältettiin confidential-taulun kohdalla. Korjattu lisäämällä eksplisiittinen
  `case when he.turvakielto then null else he.kotikunta(_nimi_fi) end` -suoja varakotikunnan
  ympärille: turvakiellon alaiset ja hetuttomat oppijat päätyvät edelleen "Ei tiedossa"
  -ryhmään, mutta muut oppijat saavat nyt parhaan tiedossa olevan (nykyisen) kotikunnan sen
  sijaan että putoaisivat turhaan "Ei tiedossa" -ryhmään.

- **Avoin kysymys 1 (pidennetty oppivelvollisuus) — löytyi vahva vastaesimerkki, EI vielä
  muutettu koodissa.** `PerusopetuksenOppijamäärätRaportti.scala` ja
  `PerusopetuksenLisäopetusOppijamäärätRaportti.scala` käyttävät `pidennetty_oppivelvollisuus`-
  kenttää suoraan omana, itsenäisenä totuusarvonaan — `toiminta_alueittain_opiskelu` ja
  `opetus_vamman_sairauden_tai_rajoitteen_perusteella` ovat näissä raporteissa erilliset,
  toisistaan riippumattomat sarakkeet, ei koskaan yhdistettynä "pidennetyksi
  oppivelvollisuudeksi". Meidän kyselymme "kuusitoista erityisen tuen perusteella" -sarake
  käyttää edelleen johdettua OR-logiikkaa — tämä on eri käsite kuin `pidennetty_oppivelvollisuus`
  kaikkien sisarraporttien perusteella. Vaatii erillisen päätöksen ennen muuttamista, koska ei
  tiedetä kumpaa ticket todella tarkoittaa "erityisen tuen perusteella" -sarakkeella.

- **Avoin kysymys 2 (kv-koulujen lukuvuosirajaus) — löytyi vastaesimerkki, EI vielä muutettu
  koodissa.** `LukioDiaIbInternationalESHOpiskelijamaaratRaportti.scala` ei rajaa
  `internationalschool`/`europeanschoolofhelsinki`-suorituksia `alkamispäivä`/lukuvuosi-ehdolla
  lainkaan — se nojaa yksin opiskeluoikeusjaksoon (`tila = 'lasna' and alku <= päivä and
  loppu >= päivä`). Meidän `pts.alkamispaiva <= $päivä` -ehtomme ei siis ole kopioitu
  mistään sisarraportista, vaan oma lisäyksemme. Voi olla tarpeeton — vaatii vahvistuksen
  ennen poistoa/muuttamista.

## 13. Yhteenveto: ratkaistut ja avoimet kysymykset

Koottu kaikista edellisistä pykälistä yhteen paikkaan, koska ne ovat hajaantuneet moneen
alaotsikkoon dokumentin kasvaessa. Tämä pykälä ei korvaa yksityiskohtia — vain kokoaa
tilannekuvan yhdelle sivulle. Päivitä tätä listaa kun jokin kohta ratkeaa/muuttuu.

### 13.1 Ratkaistut

1. Nimi ja sijainti: **Kotikuntalaskelma**, "Yleiset"-osio (2 §).
2. Rakenne: yhden päivän snapshot (ei aikaväli), data-only, ei VOS-laskentakaavaa (3 §).
3. Ikäryhmä-bucketit: 6, 7–12, 13–15, 16 (jaettu), yhteensä (5 §, kohta 4).
4. Erityisen tuen -sarakkeet (vamma, toiminta-alue, varhennettu ov, tuen päätöksen jakso,
   tavoitekokonaisuuksittain, lähdejärjestelmä, -tunniste) jätetty pois oppijakohtaisista
   sarakkeista — **mutta ks. 13.2, kohta 4**, tätä on osittain kierretty Oppijat-välilehdellä.
5. Turvakielto/confidential-taulu-virhe korjattu: käytetään julkista `r_kotikuntahistoria`-taulua,
   ei confidential-varianttia (5 §, kohta 6).
6. Kotikuntahistorian aukkokäsittely: varakotikuntana `r_henkilo.kotikunta`, eksplisiittisesti
   turvakielto-suojattuna (12 §).
7. Pääsyoikeusmalli on organisaatioskoopattu kuten muutkin VOS-raportit — ei ole
   "organisaatioton" raportti, `hasGlobalReadAccess` toimii samoin kuin sisarraporteissa (9 §).
8. Backend-endpoint toteutettu: servlet-reitti, `RaportitService`-metodi, `Kotikuntalaskelma.scala`
   (10 §). `extract()`-tyyppiepäselvyysbugi korjattu `::date`-castilla (10 §).
9. "Oppijat"-välilehti päätetty ja toteutettu jatkokokouksessa: oppijanumero + tosi/epätosi-liput
   ikäryhmäsarakkeille, korvaa 4 §:n alkuperäisen laajemman sarakelistan (10.1 §).
10. Kaksi riippumatonta tarkistuskierrosta (fixturet + backend/frontend) tehty; kaksi löydettyä
    bugia korjattu (fixturen tyyppivirhe, `muutto_pvm`-NULL-käsittely) (11 §).
11. Kaksi fixture-validaatiobugia korjattu ajonaikaisesti (toimintaAlueittainOpiskelu-jakson
    päivämäärä, puuttuva `tuenPäätöksenJaksot`) (11 §).
12. Realistisen-ikäisiä testioppijoita puuttui käytännössä kokonaan — lisätty 6 uutta, tietoisesti
    pidetty committoimattomina CI-vaikutuksen välttämiseksi (10 §).

### 13.2 Avoimet — vaatii vielä päätöksen tai toteutuksen

1. **Pidennetyn oppivelvollisuuden kanoninen kenttä.** Uutta, vahvaa evidenssiä 12 §:ssä: kaikki
   kolme tarkistettua sisarraporttia käyttävät `pidennetty_oppivelvollisuus`-kenttää suoraan,
   itsenäisenä — eivät koskaan yhdistä sitä `toiminta_alueittain_opiskelu`/
   `opetus_vamman_sairauden_tai_rajoitteen_perusteella`-kenttiin, kuten meidän kyselymme tekee.
   Vaatii päätöksen mitä ticket oikeasti tarkoittaa "erityisen tuen perusteella" -sarakkeella
   (5 §, kohta 1; 12 §).
2. **Hetuttomien esitystapa** on muodollisesti yhä auki — käytännössä toteutettu
   `'Ei tiedossa'`-fallback-ryhmänä, mutta tätä ei ole erikseen vahvistettu kokouksessa
   (5 §, kohta 2).
3. **Välilehtien tarkka layout/muotoilu** — nyt tiedetään rakenne (2 välilehteä: aggregaatti +
   Oppijat), mutta ei sarakejärjestystä, leveyksiä tms. hienosäätöä (3 §).
4. **16-vuotiaiden erityisen tuen tiedon yksilöity paljastuminen Oppijat-välilehdellä** —
   ristiriidassa 4 §:n erityisen tuen -poisjätön hengen kanssa. Nimenomaisesti kirjattu käyttäjälle
   ennen toteutusta; käyttäjä päätti toteuttaa siitä huolimatta ja käsittelee asian myöhemmin
   uudelleen (5 §, kohta 5; 10.1 §).
5. **"Ei tiedossa" -korin erottamattomuus** (turvakielto, hetuttomat ja muut resolvoitumattomat
   samassa korissa) — sivuvaikutus toteutuksesta, ei erikseen päätetty vaatimus (5 §, kohta 6).
6. **Kansainvälisten koulujen lukuvuosirajaus** (`pts.alkamispaiva <= päivä`) on oma lisäyksemme,
   ei löydy mistään sisarraportista (Lukio/ESH-raportti ei rajaa tätä ollenkaan) — voi olla
   tarpeeton, vaatii vahvistuksen ennen poistoa/muuttamista (12 §).
7. **Pääsyoikeusmallin loppuunvienti puuttuu**: uusi `RaportinTyyppi`-case-object +
   koulutusmuoto-kytkentä (perusopetus/esiopetus/internationalschool/europeanschoolofhelsinki) +
   mahdollinen `raportit.rajatut`-kytkentä. Nyt vain väliaikainen `visibleForAllOrgs`-näkyvyysshimmi
   ja yksinkertaistettu, vain-perusopetus-tarkistava käyttöoikeustarkistus servlet-reitillä (9 §).
8. **Kaksi testifixture-aukkoa**: realistisen-ikäistä (lapsi) turvakielto-oppijaa ja
   kotiopetus-poisrajaus-tapausta ei ole — olemassa olevat vastaavat fixturet ovat aikuisia (10 §).
9. **Testifixtuurien commit-status on tietoinen väliaikaisratkaisu**, ei pysyvä: fixturet pidetään
   tarkoituksella committoimattomina CI-regressiopiilotuksen välttämiseksi. Pitääkö niitä joskus
   committoida kunnolla (siivottuna, muiden testien kanssa yhteensopiviksi) on oma erillinen
   päätös (10 §).
10. **20 HTP -budjetin riittävyys** nykyisen laajuuden (kaksi välilehteä, useita koulutusmuotoja,
    pääsyoikeusmalli) toteuttamiseen — ei arvioitu missään vaiheessa tätä dokumenttia (6 §).
