import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli
    osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus>
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =
  (
    o: {
      arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
      tyyppi?: Koodistokoodiviite<
        'suorituksentyyppi',
        'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
      >
      tila?: Koodistokoodiviite<'suorituksentila', string>
      koulutusmoduuli?: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli
      osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus>
    } = {}
  ): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli:
      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli(
        {
          tunniste: Koodistokoodiviite({
            koodiarvo:
              'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus',
            koodistoUri: 'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
          })
        }
      ),
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus',
    ...o
  })

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
