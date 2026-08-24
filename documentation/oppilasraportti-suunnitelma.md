# Oppilasraportti — suunnitelma

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

**Oppilasraportti**, sijoitetaan **Yleiset-raportti**-osioon (ei omaksi koulutusmuotokohtaiseksi
raportikseen).

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

### Vielä auki / epävarma

- Ikäryhmä (esim. 6, 7–12, 13–15 jne. — bucket-rajat eivät vielä lyötyjä lukkoon)

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
2. **Hetuttomat oppijat** eivät saa kotikuntaa → rikkoo ydin-ryhmittelyn (kotikunta-pohjaisuus).
   Ratkaisematon, arkkitehtuurisesti estävä kysymys.
3. Välilehtien layout / datan esitystapa vaatii vielä suunnittelua.
4. Ikäryhmä-bucketit eivät vielä lukkoon lyötyjä.

## 6. Reunaehdot

- **20 HTP** (henkilötyöpäivää) varattu koko toteutukseen.
- Koko hanke on **"alustava speksi, jos saadaan VM:ltä rahaa"** — riippuvainen Valtiovarain-
  ministeriön rahoituksesta.

## 7. Seuraavat askeleet

- [x] Päätä otetaanko erityisen tuen -sarakkeet mukaan — **päätetty jättää pois toistaiseksi**,
      ei siis vaadi tietosuoja-arviointia tässä vaiheessa.
- [ ] Ratkaise hetuttomien oppijoiden kotikunta-ongelma (tai päätä miten heidät käsitellään
      raportissa, esim. oma "tuntematon kotikunta" -ryhmä).
- [ ] Lyö lukkoon ikäryhmä-bucketit.
- [ ] Suunnittele välilehtien tarkka layout / data-esitys.
- [ ] Odota Arin korjaus pidennetyn oppivelvollisuuden logiikkaan ja arvioi vaikutus raporttiin.

## 8. Tekninen toteutus (luonnos — ei vielä validoitu)

Raportti lukisi raportointikannasta (ei live-Koski-transaktiodatasta), samaan tapaan kuin muut
raportit (`RaportitService`/`Raportti`-trait). Karkea hahmotelma aggregaattikyselystä (ei
todennettu skeeman sarakenimiä vasten):

```sql
-- Aggregaattivälilehti: oppilasmäärä opetuksen järjestäjän x kotikunnan x ikäryhmän mukaan
-- Huom: raportointikannan tarkat taulu-/sarakenimet pitää varmistaa ennen toteutusta.
SELECT
  jarjestaja.oid            AS opetuksen_jarjestaja_oid,
  jarjestaja.nimi           AS opetuksen_jarjestaja_nimi,
  henkilo.kotikunta         AS kotikunta,
  -- ikäryhmä lasketaan valitun päivämäärän ja syntymäajan perusteella, bucket-rajat auki (ks. 4 §)
  CASE
    WHEN date_part('year', age(:paiva, henkilo.syntymaaika)) < 7 THEN '6'
    WHEN date_part('year', age(:paiva, henkilo.syntymaaika)) BETWEEN 7 AND 12 THEN '7-12'
    WHEN date_part('year', age(:paiva, henkilo.syntymaaika)) BETWEEN 13 AND 15 THEN '13-15'
    ELSE 'muu'
  END                        AS ikaryhma,
  count(DISTINCT oppija.oppija_oid) AS oppilasmaara
FROM r_opiskeluoikeus oo
JOIN r_paatason_suoritus suoritus ON suoritus.opiskeluoikeus_id = oo.id
JOIN r_henkilo henkilo          ON henkilo.oppija_oid = oo.oppija_oid
JOIN r_organisaatio jarjestaja  ON jarjestaja.oid = oo.koulutustoimija_oid
WHERE :paiva BETWEEN oo.alkamispaiva AND coalesce(oo.paattymispaiva, :paiva)
  AND henkilo.kotikunta_paiva = :paiva  -- kotikunta ratkaistu samalta päivältä
GROUP BY jarjestaja.oid, jarjestaja.nimi, henkilo.kotikunta, ikaryhma;

-- Yksilöity välilehti: sama WHERE-rajaus, ei GROUP BY — palauttaa 4 §:n "Varmat"-sarakkeet
-- rivi per oppija/opiskeluoikeus, jotta jäljitettävyys aggregaattiin säilyy.
```

Tämä on vasta luonnos ohjaamaan keskustelua siitä, mistä tauluista raportointikannassa data
löytyy — ei valmis toteutus. Ennen toteutusta pitää katsoa muiden VOS-raporttien (esim.
`AikuistenPerusopetuksenOppijamäärätRaportti`) todellinen SQL/Slick-kysely mallipohjaksi.
