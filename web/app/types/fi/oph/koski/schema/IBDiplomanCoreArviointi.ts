import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBDiplomanCoreArviointi
 *
 * @see `fi.oph.koski.schema.IBDiplomanCoreArviointi`
 */
export type IBDiplomanCoreArviointi = {
  $class: 'fi.oph.koski.schema.IBDiplomanCoreArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  päivä?: string
  hyväksytty?: boolean
}

export const IBDiplomanCoreArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  päivä?: string
  hyväksytty?: boolean
}): IBDiplomanCoreArviointi => ({
  $class: 'fi.oph.koski.schema.IBDiplomanCoreArviointi',
  ...o
})

IBDiplomanCoreArviointi.className =
  'fi.oph.koski.schema.IBDiplomanCoreArviointi' as const

export const isIBDiplomanCoreArviointi = (
  a: any
): a is IBDiplomanCoreArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanCoreArviointi'
