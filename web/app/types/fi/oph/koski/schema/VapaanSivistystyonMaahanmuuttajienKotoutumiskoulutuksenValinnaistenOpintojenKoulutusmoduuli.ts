import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli'
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli =
  (
    o: {
      tunniste?: Koodistokoodiviite<
        'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
        'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
      >
      laajuus?: LaajuusOpintopisteissä
    } = {}
  ): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli',
    tunniste: Koodistokoodiviite({
      koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus',
      koodistoUri: 'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
    }),
    ...o
  })

VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli.className =
  'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli' as const

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli'
