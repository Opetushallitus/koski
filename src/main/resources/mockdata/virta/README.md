# Virta datan käsittely

## Ongelmien selvittäminen

Ongelmien selvittämisen voi aloittaa lataamalla virkailijan käyttöliittymän yläosasta VirtaXML
linkistä Virran palauttaman XML:n. Huomioitavaa on se, että XML:ssä on sekä HETU:lla haettu Virta XML,
että oppijanumerolla haettu XML. Tästä syystä XML ei ole validi vaan se pitää pilkkoa juuri elementeistä
kahtia. Huomaa, että nämä kaksi XML:ää voivat erota toisistaan. Erojen selvittämiseen voi käyttää
[virtaXmlSplitAndPrettyPrint.py](../../../../../scripts/virtaXmlSplitAndPrettyPrint.py), jolle annetaan
käyttöliittymästä ladatta XML, jonka scripti tallentaa kahdeksi tiedostoksi, joiden erot voi helposti
tarkistaa esim. käyttäen `diff` komentoa.

### Lokaalissa ympäristö
Lokaalisti helpoin tapa on korvata jokin olemassa oleva XML tiedosto, tutkittavalla virta XML-tiedostolla,
josta olet valinnut tutkittavan juuri elementin ja poistanut toisen, sekä muuttanut hetun vastaamaan tiedoston
nimeä.

Toinen vaihtoehto on generoida hetu ja nimetä XML hetun mukaan, sekä päivittää hetu XML:ään. Tämän lisäksi
pitää lisätä henkilötiedot XML, johon voit ottaa mallia olemassa olevista.

Lokaalisti ajettaessa on myös hyvä huomata, että organisaatio Mock palauttaa hyvin rajallisesti
oppilaitoksia. Näin ollen oppilaitokset eivät yleensä vastaa tuotannossa näkyviä.

## Tietoa testi oppijoista

100869-192W (virta-testiympäristössä: 290492-9455)

Valmistunut diplomi-insinööri

250668-293Y (virta-testiympäristössä: 090888-929X)

Aalto-yliopistonäytä
    Tekn. kand., kemian tekniikka
    Dipl.ins., kemian tekniikka
Helsingin yliopisto
    Luonnont. kand., matematiikka
    Fil. maist., matematiikka
Itä-Suomen yliopisto
    Luonnont. kand., matematiikka
    Fil. maist., matematiikka

250686-102E (virta-testiympäristössä: 101291-954C)

AMK valmistunut

170691-3962 (virta-testiympäristössä: 100193-948U)

AMK keskeyttänyt

090197-411W (virta-testiympäristössä: 100292-980D)

AMK opinnot kesken

060458-331R (virta-testiympäristössä: 160980-9606)

Monimutkainen tapaus, yhdistelmä erilaisia ei tutkintoon johtavia opiskeluoikeuksia. Koottu seuraavista Virta-testiympäristön oppijoista:

270991-479L: Kotimainen opiskelijaliikkuvuus <Tyyppi>8</Tyyppi> (varsinainen AMK opiskeluoikeus kotikorkeakoulussa)
280688-242D: Täydennyskoulutus         <Tyyppi>10</Tyyppi>     (Aiempi tyyppi 2 ja 4 YO opiskeluoikeus)
251087-532T: Pätevöitymiskoulutus     <Tyyppi>11</Tyyppi>
180183-899U: Erikoistumisopinnot     <Tyyppi>12</Tyyppi>
240672-432U: Opettajan pedagogiset opinnot        <Tyyppi>14</Tyyppi>        (Aiempi tyyppi 2 ja 4 YO opiskeluoikeus)
030874-618D: Ammatillinen opettajankoulutus        <Tyyppi>15</Tyyppi>
100893-704L: Oppisopimus-tyyppinenkoulutus        <Tyyppi>16</Tyyppi>
251087-532T: Erillisoikeus         <Tyyppi>18</Tyyppi>
150191-326F: Erikoistumiskoulutus     <Tyyppi>19</Tyyppi>

200990-228R: Yksinkertainen kandin ja maisterin rakenne, jossa yksi maisterin tutkintoon sisältyvistä opinnoista on
kandintutkinnon opintoavaimella.
