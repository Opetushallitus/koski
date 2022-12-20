import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DIAOppiaineenValmistavanVaiheenLukukaudenArviointi
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukaudenArviointi`
 */
export type DIAOppiaineenValmistavanVaiheenLukukaudenArviointi = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukaudenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkodiavalmistava', string>
  päivä?: string
  hyväksytty?: boolean
}

export const DIAOppiaineenValmistavanVaiheenLukukaudenArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkodiavalmistava', string>
  päivä?: string
  hyväksytty?: boolean
}): DIAOppiaineenValmistavanVaiheenLukukaudenArviointi => ({
  $class:
    'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukaudenArviointi',
  ...o
})

export const isDIAOppiaineenValmistavanVaiheenLukukaudenArviointi = (
  a: any
): a is DIAOppiaineenValmistavanVaiheenLukukaudenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukaudenArviointi'
