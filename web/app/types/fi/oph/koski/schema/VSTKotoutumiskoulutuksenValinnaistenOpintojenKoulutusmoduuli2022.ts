import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022`
 */
export type VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
  tunniste: Koodistokoodiviite<'vstkoto2022kokonaisuus', 'valinnaisetopinnot'>
  laajuus?: LaajuusOpintopisteissä
}

export const VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 =
  (
    o: {
      tunniste?: Koodistokoodiviite<
        'vstkoto2022kokonaisuus',
        'valinnaisetopinnot'
      >
      laajuus?: LaajuusOpintopisteissä
    } = {}
  ): VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 => ({
    $class:
      'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022',
    tunniste: Koodistokoodiviite({
      koodiarvo: 'valinnaisetopinnot',
      koodistoUri: 'vstkoto2022kokonaisuus'
    }),
    ...o
  })

VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022.className =
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022' as const

export const isVSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 =>
    a?.$class ===
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
