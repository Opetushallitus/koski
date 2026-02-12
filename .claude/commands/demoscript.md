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
TOR-XXXX [Tikettiotsikko]
https://jira.eduuni.fi/browse/TOR-XXXX

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

## Tärkeää

- Tuota VAIN demoscript, älä toteuta muutoksia
- Jos branchilla ei ole muutoksia masteriin nähden, ilmoita siitä
- Jos muutosten luonne on epäselvä, kysy tarkentavia kysymyksiä ennen scriptin generointia
- Jos tikettinumero ei selviä branchin nimestä, kysy se käyttäjältä
- Älä keksi testidataa (oppija OIDeja, hetuja) tyhjästä - käytä vain sellaisia jotka löytyvät koodista (fixture-tiedostot, testit, mock-oppijat)

$ARGUMENTS
