import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =
  (
    o: {
      tunniste?: Koodistokoodiviite<
        'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
        'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
      >
      laajuus?: LaajuusOpintopisteissä
    } = {}
  ): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli',
    tunniste: Koodistokoodiviite({
      koodiarvo:
        'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus',
      koodistoUri: 'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
    }),
    ...o
  })

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
