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