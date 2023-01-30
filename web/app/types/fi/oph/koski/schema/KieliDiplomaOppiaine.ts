import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.KieliDiplomaOppiaine`
 */
export type KieliDiplomaOppiaine = {
  $class: 'fi.oph.koski.schema.KieliDiplomaOppiaine'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB' | 'CLA'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'ES' | 'FI' | 'FR'>
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export const KieliDiplomaOppiaine = (o: {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB' | 'CLA'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'ES' | 'FI' | 'FR'>
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}): KieliDiplomaOppiaine => ({
  $class: 'fi.oph.koski.schema.KieliDiplomaOppiaine',
  ...o
})

KieliDiplomaOppiaine.className =
  'fi.oph.koski.schema.KieliDiplomaOppiaine' as const

export const isKieliDiplomaOppiaine = (a: any): a is KieliDiplomaOppiaine =>
  a?.$class === 'fi.oph.koski.schema.KieliDiplomaOppiaine'
