import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBCASOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.IBCASOppiaineenArviointi`
 */
export type IBCASOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.IBCASOppiaineenArviointi'
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', 'S'>
  predicted: boolean
  hyväksytty?: boolean
}

export const IBCASOppiaineenArviointi = (o: {
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana?: Koodistokoodiviite<'arviointiasteikkoib', 'S'>
  predicted: boolean
  hyväksytty?: boolean
}): IBCASOppiaineenArviointi => ({
  arvosana: Koodistokoodiviite({
    koodiarvo: 'S',
    koodistoUri: 'arviointiasteikkoib'
  }),
  $class: 'fi.oph.koski.schema.IBCASOppiaineenArviointi',
  ...o
})

IBCASOppiaineenArviointi.className =
  'fi.oph.koski.schema.IBCASOppiaineenArviointi' as const

export const isIBCASOppiaineenArviointi = (
  a: any
): a is IBCASOppiaineenArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBCASOppiaineenArviointi'
