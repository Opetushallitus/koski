import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusTunneissa } from './LaajuusTunneissa'

/**
 * MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli`
 */
export type MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli'
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  laajuus: LaajuusTunneissa
}

export const MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli = (o: {
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  laajuus: LaajuusTunneissa
}): MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli => ({
  $class:
    'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli',
  ...o
})

export const isMuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli'
