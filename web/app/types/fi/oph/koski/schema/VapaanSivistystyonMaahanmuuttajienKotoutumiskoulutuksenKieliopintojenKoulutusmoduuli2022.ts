import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'
    tunniste: Koodistokoodiviite<'vstkoto2022kielijaviestintakoulutus', string>
    laajuus?: LaajuusOpintopisteissä
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =
  (o: {
    tunniste: Koodistokoodiviite<'vstkoto2022kielijaviestintakoulutus', string>
    laajuus?: LaajuusOpintopisteissä
  }): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 => ({
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022',
    ...o
  })

VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022.className =
  'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022' as const

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =>
    a?.$class ===
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'
