import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusOpintopisteissäTaiKursseissa } from './LaajuusOpintopisteissaTaiKursseissa'
import { LocalizedString } from './LocalizedString'

/**
 * Paikallisen lukioon valmistavan koulutuksen kurssin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenLukioonValmistavanKoulutuksenKurssi`
 */
export type PaikallinenLukioonValmistavanKoulutuksenKurssi = {
  $class: 'fi.oph.koski.schema.PaikallinenLukioonValmistavanKoulutuksenKurssi'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
  kuvaus: LocalizedString
}

export const PaikallinenLukioonValmistavanKoulutuksenKurssi = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
  kuvaus: LocalizedString
}): PaikallinenLukioonValmistavanKoulutuksenKurssi => ({
  $class: 'fi.oph.koski.schema.PaikallinenLukioonValmistavanKoulutuksenKurssi',
  ...o
})

PaikallinenLukioonValmistavanKoulutuksenKurssi.className =
  'fi.oph.koski.schema.PaikallinenLukioonValmistavanKoulutuksenKurssi' as const

export const isPaikallinenLukioonValmistavanKoulutuksenKurssi = (
  a: any
): a is PaikallinenLukioonValmistavanKoulutuksenKurssi =>
  a?.$class ===
  'fi.oph.koski.schema.PaikallinenLukioonValmistavanKoulutuksenKurssi'
