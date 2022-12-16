import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =
  (o: {
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus => ({
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo:
        'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
