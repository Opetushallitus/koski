# Generoi demoscript nykyisestä branchista

Generoi demoscript nykyisen branchin muutoksista. Demoscript on tarkoitettu viikkodemoihin, joissa kehittäjä esittelee sidosryhmille uudet ominaisuudet ja muutokset.

## Vaiheet

1. **Selvitä konteksti:**
   - Lue branchin nimi (`git rev-parse --abbrev-ref HEAD`) ja tunnista tikettinumero (esim. TOR-XXXX)
   - Lue kaikki commitit masterista eteenpäin (`git log master..HEAD --oneline`)
   - Lue koko diff (`git diff master..HEAD`)
   - Lue muuttuneet tiedostot ja ymmärrä muutosten luonne

2. **Luokittele muutoksen tyyppi** (voi olla useampi):
   - **Skeemamuutos**: Uusi kenttä/rakenne tietomallissa → ohjaa skeeman visualisointiin
   - **Validaatiomuutos**: Uusi tai muutettu validaatio → näytä sallitut ja hylätyt tapaukset
   - **Käyttöliittymämuutos**: UI-muutos → näytä virkailijan ja/tai kansalaisen näkymä
   - **Rajapintamuutos**: API-muutos → näytä esimerkki-request ja -response
   - **Raporttikantamuutos**: Uusi sarake/taulu → näytä tietokannan muutos
   - **Massaluovutus**: Muutos massaluovutusrajapintaan → näytä kysely ja tulokset
   - **Bugikorjaus**: Korjattu virhe → näytä että virhe ei enää toistu

3. **Generoi demoscript** seuraavassa muodossa:

```
### [TOR-XXXX Tikettiotsikko](https://jira.eduuni.fi/browse/TOR-XXXX)

[1–3 riviä: mitä muuttui ja mikä on konkreettinen ennen/jälkeen. Numerot, rajat ja rajaukset tähän — ei jokaisen bulletin sisään.]

[Yksi rivi: mistä demo aloitetaan — URL ja millaista oppijaa tai näkymää se vaatii.]

- **[Näkymä tai toimenpide]**: [mitä yleisön pitää huomata].
- **[Näkymä tai toimenpide]**: [havainto — myös se mikä tarkoituksella ei muuttunut, jos se rajaa aihetta].

[Jos API-kutsu, endpoint ja payload omana blokkinaan sen bulletin alla, jota se koskee:]
POST https://virkailija.testiopintopolku.fi/koski/api/[endpoint]
{
  [payload]
}

[Näytä odotettu vastaus tai oleellinen osa siitä kun mahdollista:]
{
  "kenttä": "arvo",
  ...
}
```

## Pituus

Demoscript on yksi aihe demopäivän listalla ("Demoon 12.8.2026"), joten se kilpailee ajasta muiden tikettien kanssa. Pituus seuraa muutosta, ei kiinteää budjettia:

- Yksi bullet per demottava havainto. Kapea korjaus on 2–3 bullettia; laaja muutos voi olla kymmenenkin, jos jokainen kohta on oikeasti näytettävä asia.
- Yksi bullet on 1–2 virkettä: mihin katsotaan ja mitä siitä pitäisi huomata. Kehittäjä osaa klikkailla itse — älä kirjoita jokaista välivaihetta auki, ellei polku ole epäilmeinen (esim. useampi peräkkäinen valinta esimerkkisovelluksessa tai skeemaselaimessa).
- Ennen/jälkeen-kontrasti kuuluu johdantoriveille, ei jokaiseen bullettiin. Konkreettiset numerot ja rajat ("raja nousi 60k:sta 400k:een") ovat demon ydin — älä jätä niitä pois.
- Karsi täytettä, älä sisältöä. Jos havaintoja on enemmän kuin ehtii näyttää, priorisoi näkyvimmät ja kerro lopuksi yhdellä rivillä mitä jätit pois.
- Yhdistele saman näkymän tai komponentin havainnot yhdeksi bulletiksi sen sijaan että listaisit ne erikseen.
- Sisäiset korjaukset (refaktoroinnit, testit, suorituskyky, lokalisointien siivous) eivät kuulu demoscriptiin, ellei niillä ole yleisölle näkyvää vaikutusta. Suorituskykymuutos on demottava vasta kun sen näkee jostain — statusrajapinnasta, ajoajasta tai vastaavasta.

## URL-käytännöt

- Käytä aina täysiä URL-osoitteita, ei ympäristömuuttujia
- Virkailijan puolen URL:t: `https://virkailija.testiopintopolku.fi/koski/...`
- Kansalaisen puolen URL:t: `https://testiopintopolku.fi/koski/...`
- Esimerkkioppija virkailijalle: `https://virkailija.testiopintopolku.fi/koski/oppija/[oid]` ([hetu])

## Demoympäristö ja testidata

Demot ajetaan ensisijaisesti QA-ympäristöä (testiopintopolku) vasten. Paikallinen ympäristö kelpaa, jos QA:n käyttö olisi kohtuuttoman hankalaa — esimerkiksi kun demo vaatii tarkkaan rakennetun tilanteen, jota QA:sta ei löydy eikä sinne saa vaivatta vietyä.

Kirjoita siis oletuksena QA:ta vasten. Paikallista testidataa ei ole siellä olemassa:

- **Fixture-oppijat** (`KoskiSpecificMockOppijat`, `ValpasMockOppijat`) ovat vain paikallisia ja yksikkötesteissä — `Fixtures.shouldUseFixtures` on epätosi kaikissa palvelinympäristöissä. Älä esitä niiden OIDeja tai hetuja demo-oppijoina, vaikka ne löytyisivät e2e-testeistä.
- **Mockdata** (`src/main/resources/mockdata/`) korvautuu QA:ssa oikeilla palveluilla: organisaatiohierarkia, koodistot ja henkilötiedot ovat eri. Älä viittaa mockdatan organisaatioiden nimiin, oppilaitoksiin tai muihin arvoihin.

Kirjoita sen sijaan mitä demo-oppijalta vaaditaan ja jätä OID ja hetu TODO-paikanpitäjiksi, jotka kehittäjä täyttää QA:sta demoa valmistellessaan:

Avaa QA:sta perusopetuksen oppija, jolla on vahvistettu päättötodistus:
https://virkailija.testiopintopolku.fi/koski/oppija/TODO (TODO hetu)

Kuvaa vaatimus riittävän tarkasti, että oikean oppijan löytää haulla: opiskeluoikeuden tyyppi, päätason suorituksen tyyppi ja tila sekä ne kentät joita demo koskee. Jos jokin askel vaatii tietyn tilanteen (esim. lakkautettu toimipiste tai tietty koodiarvo), kirjoita se vaatimuksena äläkä paikallisesta datasta poimittuna esimerkkinä.

Poikkeus: **dokumentoidut QA-testitunnistautujat** ovat käytettävissä sellaisenaan, koska ne tulevat testi-IdP:ltä eivätkä Kosken fixtuureista. Esimerkiksi Testitunnistajan hetu `210281-9988` (ks. `src/main/resources/documentation/omadata_oauth2.md`) toimii Oma Data -demoissa. Käytä tällaista vain kun se on dokumentaatiossa, älä päättele hetua fixtuureista.

Jos demo tehdään paikallisesti, fixture-oppijat ja mockdata ovat käytettävissä normaalisti — silloin OIDit ja hetut kirjoitetaan sellaisenaan eikä TODO-paikanpitäjiä tarvita. Merkitse ympäristö scriptin alkuun, jotta lukija tietää ettei osoite toimi QA:ssa:

Demo ajetaan paikallisesti (fixture-data). Avaa Kaisa Koululainen (220109-784L):
http://localhost:7021/koski/oppija/1.2.246.562.24.00000000007

## Tyyliohje

- Kirjoita suomeksi
- **Oleta että yleisö tuntee domainin hyvin** — älä selitä mitä käsitteet tarkoittavat, älä anna taustatietoa
- Mene suoraan asiaan: mitä tehtiin ja miten sen näyttää
- Käytä imperatiivimuotoa: "Näytä", "Avaa", "Huomaa", "Muokkaa"
- "Näytä" = kehittäjä näyttää jotain aktiivisesti
- "Huomaa" = kehittäjän pitää kiinnittää yleisön huomio johonkin havaintoon
- "Avaa" = navigoi johonkin URL:iin
- Kuvaa millainen demo-oppija tarvitaan; OID ja hetu jäävät TODO-paikanpitäjiksi (ks. Demoympäristö ja testidata)
- Jokaiselle validaatiomuutokselle näytä sekä onnistuva että epäonnistuva tapaus
- Näytä odotettu vastaus tai sen oleellinen osa JSON-muodossa kun mahdollista
- Lyhyt johdanto (1–3 riviä) siitä mitä muuttui on toivottu; domainin taustoitus ei ole (ks. Pituus)
- Näytä myös se mikä tarkoituksella ei muuttunut, kun se rajaa aihetta — esim. missä uusi kenttä ei esiinny tai mikä esitäyttö jätettiin ennalleen

## Muotoilu

- Älä rivitä tekstiä lyhyisiin riveihin (ei 80 merkin rivinvaihtoa) — anna kappaleiden olla yhdellä pitkällä rivillä
- Älä sisennä tekstiä välilyönneillä
- JSON-blokit ja URL:t omille riveilleen
- Bulletit peräkkäisinä riveinä; tyhjä rivi vain listan ja koodiblokin ympärille

## Toimitus Confluencea varten

Käyttäjä liittää sisällön Confluenceen "Import markdown" -toiminnolla, joten demoscript pitää saada ulos kopioitavassa muodossa. Tee molemmat:

1. **Kirjoita demoscript aina tiedostoon** scratchpad-hakemistoon (esim. `demoscript-<branch>.md`) ja kerro polku vastauksessa. Tämä on varsinainen toimitustapa, ja siihen voi luottaa ympäristöstä riippumatta.
2. **Yritä lisäksi kopioida leikepöydälle** `pbcopy`-komennolla. Jos komentoa ei ole tai se kaatuu, mainitse se ohimennen yhdellä rivillä äläkä yritä muita keinoja — tiedosto riittää toimitukseksi. Leikepöytä ei ole käytettävissä kaikissa kehitysympäristöissä, eikä sen puuttuminen ole vika jota pitäisi korjata.

Kirjoita sama sisältö sekä vastaukseen että tiedostoon, jottei käyttäjän tarvitse avata tiedostoa nähdäkseen tuloksen.

Markdown-muotoilusäännöt:
- Otsikko `###`-tasolla, joka on samalla linkki Jira-tikettiin: `### [TOR-XXXX Tikettiotsikko](https://jira.eduuni.fi/browse/TOR-XXXX)`
- URL:t tekstissä markdown-linkkeinä (klikattavia)
- JSON- ja API-blokit fenced code blockkeina (```)
- Inline-koodi backtick-merkeillä

## Dokumentaatio-URL:t

Käytä VAIN näitä oikeita polkuja, älä keksi omia:

- `/koski/dokumentaatio` — Yleistä
- `/koski/dokumentaatio/tietomalli` — Tietomalli
- `/koski/dokumentaatio/koodistot` — Koodistot
- `/koski/dokumentaatio/rajapinnat/opintohallintojarjestelmat` — Rajapinnat opintohallintojärjestelmille
- `/koski/dokumentaatio/rajapinnat/luovutuspalvelu` — Rajapinnat viranomaisille (luovutuspalvelu)
- `/koski/dokumentaatio/rajapinnat/palveluvayla-omadata` — Palveluväylä- ja omadata-rajapinnat
- `/koski/dokumentaatio/rajapinnat/oauth2/omadata` — OAuth2-omadata-rajapinnat
- `/koski/dokumentaatio/rajapinnat/massaluovutus/oph` — Rajapinnat Opetushallituksen palveluille
- `/koski/dokumentaatio/rajapinnat/massaluovutus/koulutuksenjarjestajat` — Massaluovutusrajapinnat koulutuksenjärjestäjille
- `/koski/dokumentaatio/rajapinnat/massaluovutus/raportit` — Koulutuksenjärjestäjän raporttien lataus massaluovutusrajapinnasta
- `/koski/dokumentaatio/rajapinnat/massaluovutus/valpas` — Massaluovutusrajapinnat oppivelvollisuuden valvontaan

Perus-URL virkailijalle: `https://virkailija.testiopintopolku.fi`
Perus-URL kansalaiselle: `https://testiopintopolku.fi`

## Tärkeää

- Tuota VAIN demoscript, älä toteuta muutoksia
- Jos branchilla ei ole muutoksia masteriin nähden, ilmoita siitä
- Jos muutosten luonne on epäselvä, kysy tarkentavia kysymyksiä ennen scriptin generointia
- Jos tikettinumero ei selviä branchin nimestä, kysy se käyttäjältä
- Älä keksi testidataa (oppija OIDeja, hetuja) tyhjästä äläkä poimi sitä fixtuureista tai mockdatasta — jätä ne TODO-paikanpitäjiksi (ks. Demoympäristö ja testidata)

$ARGUMENTS
