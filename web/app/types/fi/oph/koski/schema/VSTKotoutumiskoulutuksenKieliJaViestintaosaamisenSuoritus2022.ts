import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from './VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from './VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from './VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuoritus
 * VST-KOTO kieli- ja viestintäosaamisen osasuoritus
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022`
 */
export type VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022'
  arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli
  osasuoritukset?: Array<VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus>
}

export const VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 = (
  o: {
    arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli?: VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli
    osasuoritukset?: Array<VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus>
  } = {}
): VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'kielijaviestintaosaaminen',
      koodistoUri: 'vstkoto2022kokonaisuus'
    })
  }),
  $class:
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022',
  ...o
})

export const isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 =>
  a?.$class ===
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022'
