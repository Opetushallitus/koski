## Rajapinnat oppilashallintojärjestelmille

Tällä sivulle kuvataan rajapinnat tiedonsiirroille oppilaitoksen tietojärjestelmistä (oppilashallintojärjestelmistä) Koskeen. Rajapinnan avulla Koskeen
voi tallentaa tietoja oppijoiden opiskeluoikeuksista, opintosuorituksista ja läsnäolosta oppilaitoksissa. Rajapinnan avulla tietoja voi myös hakea ja muokata.

Tiedonsiirron rajapinta on REST-tyyppinen, ja dataformaattina on JSON.

Rajapinnan käyttö vaatii autentikoinnin ja pääsy tietoihin rajataan käyttöoikeusryhmillä.
Näin ollen esimerkiksi oikeus oppilaan tietyssä oppilaitoksessa suorittamien opintojen päivittämiseen voidaan antaa vain kyseisen oppilaitoksen järjestelmälle.

Kaikki rajapinnat vaativat HTTP Basic Authentication -tunnistautumisen, eli käytännössä `Authorization`-headerin HTTP-pyyntöön.

Rajapinnat on lueteltu ja kuvattu alla. Voit myös testata rajapintojen toimintaa tällä sivulla, kunhan käyt ensin [kirjautumassa sisään](/koski) järjestelmään.
Saat tarvittavat tunnukset Koski-kehitystiimiltä pyydettäessä.

Rajapintojen käyttämät virhekoodit on myös kuvattu alla. Virhetapauksissa rajapinnat käyttävät alla kuvattuja HTTP-statuskoodeja ja sisällyttävät tarkemmat virhekoodit ja selitteineen JSON-tyyppiseen paluuviestiin.
Samaan virhevastaukseen voi liittyä useampi virhekoodi/selite.

### Cross-site Request Forgery

Opintopolun rajapintoihin on lisätty Cross-site Request Forgery -hyökkäyksiltä suojaava parametri. Oppilashallintojärjestelmän tekemiin kutsuihin, jotka ovat tyypiltään muuta kuin GET tai OPTION, on lisättävä seuraavat asiat:

- HTTP-otsake nimeltä `CSRF`. Arvo on vapaasti määriteltävä. Suositeltava arvo on sama kuin käytetty `Caller-Id`.
- Eväste, jonka nimi on `CSRF`. Arvon on oltava sama kuin ylläolevan HTTP-otsakkeen.

Tämä vaatimus koskee virkailija-aliverkkotunnukseen tehtäviä pyyntöjä (esim. https://virkailija.opintopolku.fi). CSRF-pakotus ei ole käytössä vanhempaa koski-aliverkkotunnusta (kuten https://koski.opintopolku.fi) käyttävissä rajapinnoissa. Kenttien lisääminen on kuitenkin suositeltavaa tulevaisuuden varalta.
