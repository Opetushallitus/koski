import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * IB-lukion DP Core -kurssin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBCoreKurssi`
 */
export type IBCoreKurssi = {
  $class: 'fi.oph.koski.schema.IBCoreKurssi'
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export const IBCoreKurssi = (o: {
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}): IBCoreKurssi => ({ $class: 'fi.oph.koski.schema.IBCoreKurssi', ...o })

IBCoreKurssi.className = 'fi.oph.koski.schema.IBCoreKurssi' as const

export const isIBCoreKurssi = (a: any): a is IBCoreKurssi =>
  a?.$class === 'fi.oph.koski.schema.IBCoreKurssi'
