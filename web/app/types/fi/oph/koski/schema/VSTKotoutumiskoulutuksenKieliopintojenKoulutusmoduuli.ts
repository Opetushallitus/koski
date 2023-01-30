import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli`
 */
export type VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
  tunniste: Koodistokoodiviite<
    'vstkoto2022kokonaisuus',
    'kielijaviestintaosaaminen'
  >
  laajuus?: LaajuusOpintopisteissä
}

export const VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli = (
  o: {
    tunniste?: Koodistokoodiviite<
      'vstkoto2022kokonaisuus',
      'kielijaviestintaosaaminen'
    >
    laajuus?: LaajuusOpintopisteissä
  } = {}
): VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli => ({
  $class:
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'kielijaviestintaosaaminen',
    koodistoUri: 'vstkoto2022kokonaisuus'
  }),
  ...o
})

VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli.className =
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli' as const

export const isVSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli = (
  a: any
): a is VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =>
  a?.$class ===
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
