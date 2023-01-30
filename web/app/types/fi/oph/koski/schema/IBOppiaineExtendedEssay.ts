import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBAineRyhmäOppiaine } from './IBAineRyhmaOppiaine'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBOppiaineExtendedEssay`
 */
export type IBOppiaineExtendedEssay = {
  $class: 'fi.oph.koski.schema.IBOppiaineExtendedEssay'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'EE'>
  aine: IBAineRyhmäOppiaine
  aihe: LocalizedString
  pakollinen: boolean
}

export const IBOppiaineExtendedEssay = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetib', 'EE'>
  aine: IBAineRyhmäOppiaine
  aihe: LocalizedString
  pakollinen: boolean
}): IBOppiaineExtendedEssay => ({
  $class: 'fi.oph.koski.schema.IBOppiaineExtendedEssay',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'EE',
    koodistoUri: 'oppiaineetib'
  }),
  ...o
})

IBOppiaineExtendedEssay.className =
  'fi.oph.koski.schema.IBOppiaineExtendedEssay' as const

export const isIBOppiaineExtendedEssay = (
  a: any
): a is IBOppiaineExtendedEssay =>
  a?.$class === 'fi.oph.koski.schema.IBOppiaineExtendedEssay'
