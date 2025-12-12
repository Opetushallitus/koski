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
    kirjoittamisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    kuullunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    puhumisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    hyväksytty?: boolean
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =
  (o: {
    päivä: string
    luetunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    arvosana?: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
    kirjoittamisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    kuullunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    puhumisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    hyväksytty?: boolean
  }): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi => ({
    arvosana: Koodistokoodiviite({
      koodiarvo: 'Hyväksytty',
      koodistoUri: 'arviointiasteikkovst'
    }),
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi',
    ...o
  })

VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi.className =
  'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi' as const

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =>
    a?.$class ===
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
