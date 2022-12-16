import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * VapaanSivistystyöJotpaKoulutuksenArviointi
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyöJotpaKoulutuksenArviointi`
 */
export type VapaanSivistystyöJotpaKoulutuksenArviointi = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyöJotpaKoulutuksenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', string>
  päivä: string
  hyväksytty?: boolean
}

export const VapaanSivistystyöJotpaKoulutuksenArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', string>
  päivä: string
  hyväksytty?: boolean
}): VapaanSivistystyöJotpaKoulutuksenArviointi => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyöJotpaKoulutuksenArviointi',
  ...o
})

export const isVapaanSivistystyöJotpaKoulutuksenArviointi = (
  a: any
): a is VapaanSivistystyöJotpaKoulutuksenArviointi =>
  a?.$class === 'VapaanSivistystyöJotpaKoulutuksenArviointi'
