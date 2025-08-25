import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBCoreKurssinArviointi
 *
 * @see `fi.oph.koski.schema.IBCoreKurssinArviointi`
 */
export type IBCoreKurssinArviointi = {
  $class: 'fi.oph.koski.schema.IBCoreKurssinArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  päivä: string
  hyväksytty?: boolean
}

export const IBCoreKurssinArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  päivä: string
  hyväksytty?: boolean
}): IBCoreKurssinArviointi => ({
  $class: 'fi.oph.koski.schema.IBCoreKurssinArviointi',
  ...o
})

IBCoreKurssinArviointi.className =
  'fi.oph.koski.schema.IBCoreKurssinArviointi' as const

export const isIBCoreKurssinArviointi = (a: any): a is IBCoreKurssinArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBCoreKurssinArviointi'
