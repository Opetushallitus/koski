import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from './VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 } from './VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus } from './VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuoritus
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen valinnaisten opintojen osasuoritus
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022`
 */
export type VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
  arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022
  osasuoritukset?: Array<VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus>
}

export const VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 = (
  o: {
    arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli?: VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022
    osasuoritukset?: Array<VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus>
  } = {}
): VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli:
    VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022({
      tunniste: Koodistokoodiviite({
        koodiarvo: 'valinnaisetopinnot',
        koodistoUri: 'vstkoto2022kokonaisuus'
      })
    }),
  $class:
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022',
  ...o
})

export const isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 =>
  a?.$class === 'VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
