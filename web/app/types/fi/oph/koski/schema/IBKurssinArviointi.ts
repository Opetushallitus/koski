import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBKurssinArviointi
 *
 * @see `fi.oph.koski.schema.IBKurssinArviointi`
 */
export type IBKurssinArviointi = {
  $class: 'fi.oph.koski.schema.IBKurssinArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  päivä: string
  hyväksytty?: boolean
}

export const IBKurssinArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  päivä: string
  hyväksytty?: boolean
}): IBKurssinArviointi => ({
  $class: 'fi.oph.koski.schema.IBKurssinArviointi',
  ...o
})

export const isIBKurssinArviointi = (a: any): a is IBKurssinArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBKurssinArviointi'
