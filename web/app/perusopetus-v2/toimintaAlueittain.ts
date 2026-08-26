import { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import { isPerusopetuksenToiminta_AlueenSuoritus } from '../types/fi/oph/koski/schema/PerusopetuksenToimintaAlueenSuoritus'

/**
 * Päättelyyn tarvitaan osasuorituksesta vain tyyppitunniste, joten parametrit
 * ovat rakenteellisia – sama ratkaisu kuin oppiaineLaajuus.ts:ssä.
 */
type TyypitettyOsasuoritus = { $class: string }

/**
 * Onko opiskeluoikeudella lisätieto toiminta-alueittain opiskelusta.
 *
 * Opiskeluoikeustasoinen ja päivämäärätön: myös päättynyt jakso tai vanhentunut
 * erityisen tuen päätös pitää lipun päällä. Tarkoitettu tilanteisiin, joissa
 * osasuorituksista ei ole vielä mitään pääteltävissä – ennen kaikkea uuden
 * vuosiluokan esitäyttöön (UusiPerusopetuksenVuosiluokanSuoritusModal).
 *
 * Näytettävän taulukon tila päätellään suorituskohtaisesti, ks.
 * isToimintaAlueittainSuoritus.
 */
export const isToimintaAlueittainOpiskelu = (
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus
): boolean => {
  const lisätiedot = opiskeluoikeus.lisätiedot
  if (!lisätiedot) return false

  if (lisätiedot.erityisenTuenPäätös?.opiskeleeToimintaAlueittain) {
    return true
  }

  if (
    lisätiedot.erityisenTuenPäätökset?.some(
      (p) => p.opiskeleeToimintaAlueittain
    )
  ) {
    return true
  }

  return Boolean(
    lisätiedot.toimintaAlueittainOpiskelu &&
    lisätiedot.toimintaAlueittainOpiskelu.length > 0
  )
}

/** Sisältääkö lista toiminta-aluesuorituksia. */
export const sisältääToimintaAlueita = (
  osasuoritukset: TyypitettyOsasuoritus[]
): boolean => osasuoritukset.some(isPerusopetuksenToiminta_AlueenSuoritus)

/**
 * Sisältääkö lista tavallisia oppiaineita.
 *
 * Tarkistus tehdään negaationa: tyyppivartijat vertaavat $class-merkkijonoa
 * tarkasti, joten positiivinen muoto luokittelisi unioniin mahdollisesti
 * lisättävän uuden jäsenen hiljaisesti toiminta-alueeksi.
 */
export const sisältääOppiaineita = (
  osasuoritukset: TyypitettyOsasuoritus[]
): boolean =>
  osasuoritukset.some((s) => !isPerusopetuksenToiminta_AlueenSuoritus(s))

/**
 * Sekamuotoinen lista sisältää sekä oppiaineita että toiminta-alueita.
 *
 * Perusopetuksen opetussuunnitelman perusteiden mukaan toiminta-alueittain
 * opiskellaan oppiainejaon sijaan, joten sekamuoto ei ole tuettu tila. Sitä
 * syntyy silti tiedonsiirrosta ja kesken lukuvuoden tehdyistä siirtymistä, ja
 * käyttöliittymän on esitettävä se ymmärrettävästi (TOR-2587).
 */
export const isSekamuotoinen = (
  osasuoritukset: TyypitettyOsasuoritus[]
): boolean =>
  sisältääOppiaineita(osasuoritukset) && sisältääToimintaAlueita(osasuoritukset)

/**
 * Mitä osasuorituslista sisältää. Otsikot johdetaan tästä eikä
 * lisätietolipusta: taulukon on kuvattava sitä, mitä siinä oikeasti on.
 * Tuotannossa on 1444 päätason suoritusta, joilla on pelkkiä toiminta-alueita
 * mutta ei lippua - lippuun sidottu otsikko valehtelisi niistä jokaisesta.
 */
export type OsasuoritustenSisältö =
  'oppiaineet' | 'toimintaAlueet' | 'sekamuotoinen' | 'tyhjä'

export const osasuoritustenSisältö = (
  osasuoritukset: TyypitettyOsasuoritus[]
): OsasuoritustenSisältö => {
  const oppiaineita = sisältääOppiaineita(osasuoritukset)
  const toimintaAlueita = sisältääToimintaAlueita(osasuoritukset)
  if (oppiaineita && toimintaAlueita) return 'sekamuotoinen'
  if (toimintaAlueita) return 'toimintaAlueet'
  if (oppiaineita) return 'oppiaineet'
  return 'tyhjä'
}

/**
 * Onko taulukko toiminta-aluetaulukko, kun sisältö ja tyhjän listan oletus
 * tunnetaan.
 *
 * Sisältö ratkaisee: taulukon otsikoiden, sarakkeen ja lisäyspudotuksen on
 * kuvattava sitä, mitä listalla oikeasti on. Sekamuoto ei ole tuettu tapaus
 * (tuoteomistajan vahvistus), joten se ei ole toiminta-aluetaulukko eikä
 * oppiainetaulukko - kutsuja käsittelee sen erikseen.
 *
 * Tyhjällä listalla ei ole mitään pääteltävää, jolloin ratkaisee `tyhjänOletus`:
 * lisätietolippu, tai käyttäjän valinta silloin kun käyttöliittymä tarjoaa sen.
 */
export const toimintaAlueTaulukko = (
  sisältö: OsasuoritustenSisältö,
  tyhjänOletus: boolean
): boolean =>
  sisältö === 'toimintaAlueet' || (sisältö === 'tyhjä' && tyhjänOletus)

/** Sama sääntö, kun tyhjän listan oletus otetaan lisätietolipusta. */
export const isToimintaAlueittainSuoritus = (
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus,
  osasuoritukset: TyypitettyOsasuoritus[]
): boolean =>
  toimintaAlueTaulukko(
    osasuoritustenSisältö(osasuoritukset),
    isToimintaAlueittainOpiskelu(opiskeluoikeus)
  )
