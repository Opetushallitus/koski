import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönJotpaKoulutuksenOsasuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuoritus`
 */
export type VapaanSivistystyönJotpaKoulutuksenOsasuoritus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuoritus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusOpintopisteissä
}

export const VapaanSivistystyönJotpaKoulutuksenOsasuoritus = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusOpintopisteissä
}): VapaanSivistystyönJotpaKoulutuksenOsasuoritus => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuoritus',
  ...o
})

VapaanSivistystyönJotpaKoulutuksenOsasuoritus.className =
  'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuoritus' as const

export const isVapaanSivistystyönJotpaKoulutuksenOsasuoritus = (
  a: any
): a is VapaanSivistystyönJotpaKoulutuksenOsasuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuoritus'
