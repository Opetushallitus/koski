import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
    >
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =
  (
    o: {
      koulutusmoduuli?: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli
      tyyppi?: Koodistokoodiviite<
        'suorituksentyyppi',
        'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
      >
      arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
      tila?: Koodistokoodiviite<'suorituksentila', string>
    } = {}
  ): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli:
      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli(
        {
          tunniste: Koodistokoodiviite({
            koodiarvo:
              'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus',
            koodistoUri: 'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
          })
        }
      ),
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus',
    ...o
  })

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
