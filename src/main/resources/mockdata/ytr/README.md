Tämä hakemisto sisältää seuraavia YTR mockidatoja:

`oph-koski` sisältää ko. YTR rajapinnan palauttamia oppijoita, joita haetaan, kun Kosken rajapinnasta haetaan oppijaa.
Näiden oppijoiden Ylioppilastutkintotiedot näkyvät siis Koksen virkailjanäkymässä.

`oph-registrydata` on rajapinta, josta haetaan isompi määrä oppijoita kerralla, syntymäajan tai muutospäivämäärän mukaan.
Näitä ladataan Kosken kantaan ja käytetään mm. raportointikannan generointiin. Kosken oppijarajapinta ei käytä suoraan
näitä tietoja. Eli jos molempia rajapintoja haluaa testata niin oppijan pitää olla molemmissa hakemistoissa oikeassa
muodossa.

Jos tänne lisää uuden oppijan niin hetu on myös lisättävä MockYtrClientin `mockOphRegistrydataHetus` muuttujaan.

`oph-registrydata` hakemistossa voi olla useita versioita samasta oppijasta `_<numero>` suffiksilla. MockYtrClientin saa
palauttamaan näitä `incrementOppijaVersion()` kutsulla.

`modifiedSince` sisältää hetut jotka mockYtrClient palauttaa kun ko. rajapinnasta kysellään tiettyä päivämäärää.

