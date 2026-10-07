import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'

// Uuden käyttöliittymän editori tallentaa opiskeluoikeuden ja päivittää vain
// oman lomakkeensa. UiAdapter pitää sivun oppijadatan muistissa ja kiinnittää
// editorit siitä uudelleen esimerkiksi opiskeluoikeustyypin välilehteä
// vaihdettaessa, joten sille välitetään tallennettu versio. Muuten editori
// alustuisi tallennusta edeltävillä tiedoilla.
type Kuuntelija = (opiskeluoikeus: Opiskeluoikeus) => void

const kuuntelijat = new Set<Kuuntelija>()

export const kuunteleTallennettujaOpiskeluoikeuksia = (
  kuuntelija: Kuuntelija
): (() => void) => {
  kuuntelijat.add(kuuntelija)
  return () => {
    kuuntelijat.delete(kuuntelija)
  }
}

export const ilmoitaTallennettuOpiskeluoikeus = (
  opiskeluoikeus: Opiskeluoikeus
): void => kuuntelijat.forEach((kuuntelija) => kuuntelija(opiskeluoikeus))
