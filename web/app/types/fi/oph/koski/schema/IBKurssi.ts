import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusOpintopisteissäTaiKursseissa } from './LaajuusOpintopisteissaTaiKursseissa'

/**
 * IB-lukion kurssin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBKurssi`
 */
export type IBKurssi = {
  $class: 'fi.oph.koski.schema.IBKurssi'
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
}

export const IBKurssi = (o: {
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
}): IBKurssi => ({ $class: 'fi.oph.koski.schema.IBKurssi', ...o })

IBKurssi.className = 'fi.oph.koski.schema.IBKurssi' as const

export const isIBKurssi = (a: any): a is IBKurssi =>
  a?.$class === 'fi.oph.koski.schema.IBKurssi'
