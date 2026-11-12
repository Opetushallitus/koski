import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDiplomanKieliOppiaine`
 */
export type IBDiplomanKieliOppiaine = {
  $class: 'fi.oph.koski.schema.IBDiplomanKieliOppiaine'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB' | 'CLA'>
  taso: Koodistokoodiviite<'oppiaineentasoib', string>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  iboOppiainekoodi?: string
}

export const IBDiplomanKieliOppiaine = (o: {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB' | 'CLA'>
  taso: Koodistokoodiviite<'oppiaineentasoib', string>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  iboOppiainekoodi?: string
}): IBDiplomanKieliOppiaine => ({
  $class: 'fi.oph.koski.schema.IBDiplomanKieliOppiaine',
  ...o
})

IBDiplomanKieliOppiaine.className =
  'fi.oph.koski.schema.IBDiplomanKieliOppiaine' as const

export const isIBDiplomanKieliOppiaine = (
  a: any
): a is IBDiplomanKieliOppiaine =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanKieliOppiaine'
