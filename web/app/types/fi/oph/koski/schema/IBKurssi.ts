import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusKursseissa } from './LaajuusKursseissa'

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
  laajuus?: LaajuusKursseissa
}

export const IBKurssi = (o: {
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}): IBKurssi => ({ $class: 'fi.oph.koski.schema.IBKurssi', ...o })

IBKurssi.className = 'fi.oph.koski.schema.IBKurssi' as const

export const isIBKurssi = (a: any): a is IBKurssi =>
  a?.$class === 'fi.oph.koski.schema.IBKurssi'
