import * as Eq from 'fp-ts/Eq'

type TunnistettavaPäätasonSuoritus = {
  $class: string
  koulutusmoduuli: { tunniste: { koodiarvo: string } }
  alkamispäivä?: string
}

const luokka = (s: TunnistettavaPäätasonSuoritus): string | undefined =>
  'luokka' in s && typeof s.luokka === 'string' ? s.luokka : undefined

/**
 * Tunnistaa poistettavan päätason suorituksen ladatusta opiskeluoikeudesta
 * (useRemovePäätasonSuoritus). Indeksi ei kelpaa, koska muokkauksessa lisätty
 * vuosiluokka lajitellaan tallennettujen väliin. Saman vuosiluokan suoritukset
 * (luokalle jääminen) erotetaan luokan ja alkamispäivän perusteella.
 */
export const perusopetuksenPäätasonSuoritusEq: Eq.Eq<TunnistettavaPäätasonSuoritus> =
  {
    equals: (a, b) =>
      a.$class === b.$class &&
      a.koulutusmoduuli.tunniste.koodiarvo ===
        b.koulutusmoduuli.tunniste.koodiarvo &&
      a.alkamispäivä === b.alkamispäivä &&
      luokka(a) === luokka(b)
  }
