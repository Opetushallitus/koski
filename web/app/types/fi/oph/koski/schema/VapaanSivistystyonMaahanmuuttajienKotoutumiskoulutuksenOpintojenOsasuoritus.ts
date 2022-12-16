import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
    tunniste: PaikallinenKoodi
    kuvaus: LocalizedString
    laajuus?: LaajuusOpintopisteissä
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus =
  (o: {
    tunniste: PaikallinenKoodi
    kuvaus: LocalizedString
    laajuus?: LaajuusOpintopisteissä
  }): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus => ({
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus',
    ...o
  })

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
