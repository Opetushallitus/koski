import { Aikajakso } from '../../types/fi/oph/koski/schema/Aikajakso'
import { ErityisenTuenPäätös } from '../../types/fi/oph/koski/schema/ErityisenTuenPaatos'
import { Tukijakso } from '../../types/fi/oph/koski/schema/Tukijakso'

/**
 * Lisätietojen "Lisää"-painikkeiden luomat tyhjät jaksot.
 *
 * Sääntö: lisätietoihin lisättävän jakson päivämääräkentät ovat tyhjiä.
 * Käyttöliittymä ei keksi käyttäjän puolesta päivämäärää, jota tämä ei ole
 * kirjoittanut. Sääntö koskee kaikkien koulutusmuotojen lisätietoja:
 * perusopetus, Ahvenanmaa, ammatillinen, IB ja VST.
 *
 * Tämä poikkeaa tarkoituksella vanhasta käyttöliittymästä, jossa uusi rivi tuli
 * palvelimen prototyypistä ja pakollinen päivämäärä esitäytettiin arvolla
 * LocalDate.now (EditorModelBuilder.DateModelBuilder). Esitäytöstä luovuttiin
 * tuoteomistajan päätöksellä: kuluva päivä on harvoin jakson oikea alku, ja
 * väärä esitäytetty arvo tallentuu huomaamatta – tyhjä kenttä ei.
 *
 * Rajaus on tietoinen: muualla v2-käyttöliittymässä esitäyttö jää ennalleen
 * (mm. suorituksen vahvistuspäivä, opiskeluoikeuden tilajakson alkupäivä,
 * ammatillisen järjestämismuoto- ja työssäoppimisjaksot). Arvosanaa valittaessa
 * leimattavaa arviointipäivää ei voi edes jättää tyhjäksi: sille ei ole
 * syöttökenttää useimmissa editoreissa, ja se on TPO:n, VST:n osaamismerkin ja
 * ammatillisen arvioinneissa pakollinen kenttä, joten tyhjänä arviointia ei
 * saisi tallennettua lainkaan (ks. ArvosanaField).
 *
 * Seuraus: Aikajakso-rivi on lisäyshetkellä epätäydellinen, koska sen alkupäivä
 * on pakollinen. Kenttä näyttää virheen "Kenttä ei voi olla tyhjä" ja Tallenna
 * pysyy lukossa, kunnes päivämäärä täytetään tai rivi poistetaan. Tämä on
 * tarkoituksellista: keskeneräistä jaksoa ei saa tallentaa.
 *
 * V2-editoreissa jokainen Lisää-painike rakentaa olion itse, joten sääntö
 * pidetään täällä yhdessä paikassa. Muuten se rapautuu riviltä toiselle, kuten
 * kävi ennen TOR-2596:ta: osa riveistä esitäytti kuluvan päivän ja osa loi
 * rivin arvolla alku: '' ilman että kumpikaan oli päätetty.
 *
 * Nämä ovat funktioita eivätkä vakioita, jotta jokainen lisäys saa oman
 * olionsa eikä jaettua viitettä.
 */

/** `Aikajakso.alku` on pakollinen, mutta jätetään käyttäjän täytettäväksi. */
export const uusiTyhjäAikajakso = (): Aikajakso => Aikajakso({ alku: '' })

/** `Tukijakso.alku` on valinnainen → jätetään pois. */
export const uusiTukijakso = (): Tukijakso => Tukijakso()

/** `ErityisenTuenPäätös.alku` on valinnainen → jätetään pois. */
export const uusiErityisenTuenPäätös = (): ErityisenTuenPäätös =>
  ErityisenTuenPäätös({ opiskeleeToimintaAlueittain: false })
