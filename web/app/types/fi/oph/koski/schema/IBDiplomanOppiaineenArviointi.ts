import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBDiplomanOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.IBDiplomanOppiaineenArviointi`
 */
export type IBDiplomanOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.IBDiplomanOppiaineenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  päivä?: string
  hyväksytty?: boolean
}

export const IBDiplomanOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  päivä?: string
  hyväksytty?: boolean
}): IBDiplomanOppiaineenArviointi => ({
  $class: 'fi.oph.koski.schema.IBDiplomanOppiaineenArviointi',
  ...o
})

IBDiplomanOppiaineenArviointi.className =
  'fi.oph.koski.schema.IBDiplomanOppiaineenArviointi' as const

export const isIBDiplomanOppiaineenArviointi = (
  a: any
): a is IBDiplomanOppiaineenArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanOppiaineenArviointi'
