import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBDiplomanOppiaine } from './IBDiplomanOppiaine'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDiplomanExtendedEssay`
 */
export type IBDiplomanExtendedEssay = {
  $class: 'fi.oph.koski.schema.IBDiplomanExtendedEssay'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'EE'>
  aine?: IBDiplomanOppiaine
}

export const IBDiplomanExtendedEssay = (
  o: {
    tunniste?: Koodistokoodiviite<'oppiaineetib', 'EE'>
    aine?: IBDiplomanOppiaine
  } = {}
): IBDiplomanExtendedEssay => ({
  $class: 'fi.oph.koski.schema.IBDiplomanExtendedEssay',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'EE',
    koodistoUri: 'oppiaineetib'
  }),
  ...o
})

IBDiplomanExtendedEssay.className =
  'fi.oph.koski.schema.IBDiplomanExtendedEssay' as const

export const isIBDiplomanExtendedEssay = (
  a: any
): a is IBDiplomanExtendedEssay =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanExtendedEssay'
