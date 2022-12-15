import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus`
 */
export type VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus'
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
}

export const VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus = (o: {
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
}): VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus => ({
  $class:
    'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus',
  ...o
})

export const isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus = (
  a: any
): a is VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus =>
  a?.$class === 'VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus'
