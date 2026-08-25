# Kotikuntaraportti — suunnitelma

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

**Kotikuntaraportti**, sijoitetaan **Yleiset-raportti**-osioon (ei omaksi koulutusmuotokohtaiseksi
raportikseen). ("Oppilasraportti" oli vain kokouksessa käytetty työnimi — tiketin virallinen nimi
on Kotikuntaraportti; koodi ja lokalisointiavaimet on nimetty sen mukaan.)

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
  2. Yksilöity välilehti — kaikki oppilaat yksilöity, näkyy myös mihin lukuihin kukin on laskettu
     mukaan (jäljitettävyys aggregaattiin)
- Välilehtien tarkka asettelu / datan esitystapa on vielä auki, vaatii lisäsuunnittelua.
- Yksilötasolla täytyy näkyä, missä oppilaitoksessa oppilas opiskelee.
- **Data-only** — ei euromääräistä VOS-laskentakaavaa Koskessa. "Vossin muutokset" (varsinainen
  rahoituskaavan laskenta) käsitellään myöhemmin, erikseen.

## 4. Oppijakohtaiset sarakkeet

### Varmat

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
16-vuotiaiden alaryhmä näkyä myös yksilöidyllä välilehdellä jäljitettävyyden vuoksi.

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
taulukon sijaan. Tämä tarkoittaa, että Kotikuntaraportti on rakenteeltaan **täsmälleen sama
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
tätä varten oma `KotikuntaraporttiPaivalta.jsx`-komponentti ilman organisaatiovalitsinta — se on
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

Aggregaattivälilehden backend on nyt toteutettu (`Kotikuntaraportti.scala`, `RaportitService.
kotikuntaraportti`, `RaportitServlet` `/kotikuntaraportti`-reitti) 8.1 §:n kyselyn pohjalta,
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

- `kotikuntaraporttiKuusivuotias` (s. 2020, kotikunta Jyväskylä) — 6-vuotiaiden ryhmä
- `kotikuntaraporttiSeitsemanKaksitoista` (s. 2017, Helsinki) — 7-12-vuotiaiden ryhmä
- `kotikuntaraporttiKolmetoistaViisitoista` (s. 2012, Helsinki) — 13-15-vuotiaiden ryhmä
- `kotikuntaraporttiKuusitoistaErityinen` (s. 2010, Jyväskylä) — 16 v., `toimintaAlueittainOpiskelu`
  asetettu → testaa "erityisen tuen perusteella" -alaryhmää
- `kotikuntaraporttiKuusitoistaEiErityista` (s. 2010, Helsinki) — 16 v., ei erityisen tuen lippuja
- `kotikuntaraporttiHetuton` (s. 2015, ei hetua, ei kotikuntaa) — testaa "Ei tiedossa"-ryhmää
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
