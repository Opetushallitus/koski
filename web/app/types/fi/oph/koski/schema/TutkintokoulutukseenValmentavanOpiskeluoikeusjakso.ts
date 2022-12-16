import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeusjakso`
 */
export type TutkintokoulutukseenValmentavanOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
    | 'loma'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6' | '10'>
}

export const TutkintokoulutukseenValmentavanOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
    | 'loma'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6' | '10'>
}): TutkintokoulutukseenValmentavanOpiskeluoikeusjakso => ({
  $class:
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeusjakso',
  ...o
})

export const isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeusjakso =>
  a?.$class === 'TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
