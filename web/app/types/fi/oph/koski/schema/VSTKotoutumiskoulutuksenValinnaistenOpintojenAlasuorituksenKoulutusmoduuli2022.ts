import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022`
 */
export type VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 =
  {
    $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
    kuvaus: LocalizedString
    tunniste: PaikallinenKoodi
    laajuus?: LaajuusOpintopisteissä
  }

export const VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 =
  (o: {
    kuvaus: LocalizedString
    tunniste: PaikallinenKoodi
    laajuus?: LaajuusOpintopisteissä
  }): VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 => ({
    $class:
      'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022',
    ...o
  })

VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022.className =
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022' as const

export const isVSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 =>
    a?.$class ===
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
