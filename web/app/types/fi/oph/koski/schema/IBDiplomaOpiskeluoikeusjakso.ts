import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.IBDiplomaOpiskeluoikeusjakso`
 */
export type IBDiplomaOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'valmistunut' | 'mitatoity'
  >
}

export const IBDiplomaOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'valmistunut' | 'mitatoity'
  >
}): IBDiplomaOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeusjakso',
  ...o
})

IBDiplomaOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.IBDiplomaOpiskeluoikeusjakso' as const

export const isIBDiplomaOpiskeluoikeusjakso = (
  a: any
): a is IBDiplomaOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeusjakso'
