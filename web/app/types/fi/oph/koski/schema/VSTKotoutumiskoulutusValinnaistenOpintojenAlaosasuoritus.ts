import { VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 } from './VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuorituksen alaosasuoritus
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus`
 */
export type VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'
  koulutusmoduuli: VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus = (o: {
  koulutusmoduuli: VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus => ({
  $class:
    'fi.oph.koski.schema.VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo:
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus.className =
  'fi.oph.koski.schema.VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus' as const

export const isVSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus = (
  a: any
): a is VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'
