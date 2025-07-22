import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBDPCoreAineRyhmäOppiaine } from './IBDPCoreAineRyhmaOppiaine'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDPCoreOppiaineExtendedEssay`
 */
export type IBDPCoreOppiaineExtendedEssay = {
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineExtendedEssay'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'EE'>
  aine: IBDPCoreAineRyhmäOppiaine
  aihe: LocalizedString
  pakollinen: boolean
}

export const IBDPCoreOppiaineExtendedEssay = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetib', 'EE'>
  aine: IBDPCoreAineRyhmäOppiaine
  aihe: LocalizedString
  pakollinen: boolean
}): IBDPCoreOppiaineExtendedEssay => ({
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineExtendedEssay',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'EE',
    koodistoUri: 'oppiaineetib'
  }),
  ...o
})

IBDPCoreOppiaineExtendedEssay.className =
  'fi.oph.koski.schema.IBDPCoreOppiaineExtendedEssay' as const

export const isIBDPCoreOppiaineExtendedEssay = (
  a: any
): a is IBDPCoreOppiaineExtendedEssay =>
  a?.$class === 'fi.oph.koski.schema.IBDPCoreOppiaineExtendedEssay'
