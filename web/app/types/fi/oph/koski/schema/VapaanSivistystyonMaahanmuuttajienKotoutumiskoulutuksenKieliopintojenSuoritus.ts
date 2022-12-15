import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
    >
    arviointi?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =
  (
    o: {
      koulutusmoduuli?: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli
      tyyppi?: Koodistokoodiviite<
        'suorituksentyyppi',
        'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
      >
      arviointi?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi>
      tila?: Koodistokoodiviite<'suorituksentila', string>
    } = {}
  ): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo:
        'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli:
      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli(
        {
          tunniste: Koodistokoodiviite({
            koodiarvo:
              'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus',
            koodistoUri: 'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
          })
        }
      ),
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus',
    ...o
  })

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
