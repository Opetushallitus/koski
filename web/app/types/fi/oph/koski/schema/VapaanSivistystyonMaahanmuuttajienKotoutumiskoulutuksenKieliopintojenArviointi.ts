import { VSTKehittyvänKielenTaitotasonArviointi } from './VSTKehittyvanKielenTaitotasonArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
    päivä: string
    luetunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
    puhumisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    hyväksytty?: boolean
    kirjoittamisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    kuullunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =
  (o: {
    päivä: string
    luetunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    arvosana?: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
    puhumisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    hyväksytty?: boolean
    kirjoittamisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    kuullunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
  }): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi => ({
    arvosana: Koodistokoodiviite({
      koodiarvo: 'Hyväksytty',
      koodistoUri: 'arviointiasteikkovst'
    }),
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi',
    ...o
  })

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =>
    a?.$class ===
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
