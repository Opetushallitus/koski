import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi } from './VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenArviointi'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuorituksen alaosasuoritus
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus`
 */
export type VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenkielitaitojensuoritus'
  >
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022
  arviointi?: Array<VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenkielitaitojensuoritus'
    >
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022
    arviointi?: Array<VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }): VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus => ({
    $class:
      'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo:
        'vstmaahanmuuttajienkotoutumiskoulutuksenkielitaitojensuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

export const isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus = (
  a: any
): a is VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus'
