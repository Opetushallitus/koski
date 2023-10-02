import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.EBOpiskeluoikeusjakso`
 */
export type EBOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.EBOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'eronnut' | 'lasna' | 'mitatoity' | 'valmistunut'
  >
}

export const EBOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'eronnut' | 'lasna' | 'mitatoity' | 'valmistunut'
  >
}): EBOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.EBOpiskeluoikeusjakso',
  ...o
})

EBOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.EBOpiskeluoikeusjakso' as const

export const isEBOpiskeluoikeusjakso = (a: any): a is EBOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.EBOpiskeluoikeusjakso'
