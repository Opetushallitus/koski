import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpiskeluoikeusjakso`
 */
export type KorkeakoulunOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeusjakso'
  alku: string
  nimi?: LocalizedString
  tila: Koodistokoodiviite<'virtaopiskeluoikeudentila', string>
}

export const KorkeakoulunOpiskeluoikeusjakso = (o: {
  alku: string
  nimi?: LocalizedString
  tila: Koodistokoodiviite<'virtaopiskeluoikeudentila', string>
}): KorkeakoulunOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeusjakso',
  ...o
})

export const isKorkeakoulunOpiskeluoikeusjakso = (
  a: any
): a is KorkeakoulunOpiskeluoikeusjakso =>
  a?.$class === 'KorkeakoulunOpiskeluoikeusjakso'
