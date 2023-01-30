import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli'
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli =
  (
    o: {
      tunniste?: Koodistokoodiviite<
        'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
        'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
      >
      laajuus?: LaajuusOpintopisteissä
    } = {}
  ): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli',
    tunniste: Koodistokoodiviite({
      koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus',
      koodistoUri: 'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
    }),
    ...o
  })

VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli.className =
  'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli' as const

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli'
