import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso`
 */
export type KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'lasna' | 'hyvaksytystisuoritettu' | 'paattynyt' | 'mitatoity'
  >
}

export const KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'lasna' | 'hyvaksytystisuoritettu' | 'paattynyt' | 'mitatoity'
  >
}): KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso => ({
  $class:
    'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso',
  ...o
})

KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso' as const

export const isKielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso = (
  a: any
): a is KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso =>
  a?.$class ===
  'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso'
