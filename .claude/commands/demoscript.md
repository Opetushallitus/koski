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

[Askel-askeleelta ohjeet demoon.]

Näytä, että [jokin asia on muuttunut/lisätty].

[Jos API-kutsu, näytä endpoint ja payload suoraan:]
POST https://virkailija.testiopintopolku.fi/koski/api/[endpoint]
{
  [payload]
}

Huomaa, että [konkreettinen havainto].

[Näytä odotettu vastaus tai oleellinen osa siitä kun mahdollista:]
{
  "kenttä": "arvo",
  ...
}
```

## URL-käytännöt

- Käytä aina täysiä URL-osoitteita, ei ympäristömuuttujia
- Virkailijan puolen URL:t: `https://virkailija.testiopintopolku.fi/koski/...`
- Kansalaisen puolen URL:t: `https://testiopintopolku.fi/koski/...`
- Esimerkkioppija virkailijalle: `https://virkailija.testiopintopolku.fi/koski/oppija/[oid]` ([hetu])

## JSON schema viewer

- Jos muutos koskee skeemaa, linkitä JSON schema vieweriin
- Suoran node-linkin (esim. `#viewer-page?v=1-0-15-8-25-0`) muodostaminen koodista ei ole mahdollista
- Kirjoita placeholder: `Näytä skeemasta: https://virkailija.testiopintopolku.fi/koski/json-schema-viewer#viewer-page?v=TODO ([kentän nimi])`
- Kehittäjä täyttää oikean polun manuaalisesti

## Tyyliohje

- Kirjoita suomeksi
- **Oleta että yleisö tuntee domainin hyvin** — älä selitä mitä käsitteet tarkoittavat, älä anna taustatietoa
- Mene suoraan asiaan: mitä tehtiin ja miten sen näyttää
- Käytä imperatiivimuotoa: "Näytä", "Avaa", "Huomaa", "Muokkaa"
- "Näytä" = kehittäjä näyttää jotain aktiivisesti
- "Huomaa" = kehittäjän pitää kiinnittää yleisön huomio johonkin havaintoon
- "Avaa" = navigoi johonkin URL:iin
- Mainitse esimerkkioppijan OID ja hetu jos ne löytyvät testidatasta tai muutoksista
- Jokaiselle validaatiomuutokselle näytä sekä onnistuva että epäonnistuva tapaus
- Näytä odotettu vastaus tai sen oleellinen osa JSON-muodossa kun mahdollista
- Pidä script tiiviinä — ei johdantoja, vain askeleet

## Muotoilu

- Älä rivitä tekstiä lyhyisiin riveihin (ei 80 merkin rivinvaihtoa) — anna kappaleiden olla yhdellä pitkällä rivillä
- Älä sisennä tekstiä välilyönneillä
- JSON-blokit ja URL:t omille riveilleen
- Käytä tyhjää riviä erottamaan askeleet toisistaan

## Leikepöydälle Confluencea varten

Demoscriptin generoinnin jälkeen kopioi sama sisältö leikepöydälle `pbcopy`-komennolla (Bash-työkalulla, heredoc-syntaksilla). Käyttäjä liittää sisällön Confluenceen "Import markdown" -toiminnolla.

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
- Älä keksi testidataa (oppija OIDeja, hetuja) tyhjästä - käytä vain sellaisia jotka löytyvät koodista (fixture-tiedostot, testit, mock-oppijat)

$ARGUMENTS
