import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso`
 */
export type OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'lasna'
    | 'valiaikaisestikeskeytynyt'
    | 'katsotaaneronneeksi'
    | 'valmistunut'
    | 'mitatoity'
  >
}

export const OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso =
  (o: {
    alku: string
    tila: Koodistokoodiviite<
      'koskiopiskeluoikeudentila',
      | 'lasna'
      | 'valiaikaisestikeskeytynyt'
      | 'katsotaaneronneeksi'
      | 'valmistunut'
      | 'mitatoity'
    >
  }): OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso => ({
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso',
    ...o
  })

OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso' as const

export const isOppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso =>
    a?.$class ===
    'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso'
