import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022`
 */
export type VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
  tunniste: Koodistokoodiviite<'vstkoto2022kokonaisuus', 'ohjaus'>
  laajuus: LaajuusOpintopisteissä
}

export const VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 = (o: {
  tunniste?: Koodistokoodiviite<'vstkoto2022kokonaisuus', 'ohjaus'>
  laajuus: LaajuusOpintopisteissä
}): VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 => ({
  $class:
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'ohjaus',
    koodistoUri: 'vstkoto2022kokonaisuus'
  }),
  ...o
})

VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022.className =
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022' as const

export const isVSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 =>
  a?.$class ===
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
