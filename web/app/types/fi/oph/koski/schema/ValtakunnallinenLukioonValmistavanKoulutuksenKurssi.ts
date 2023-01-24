import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissäTaiKursseissa } from './LaajuusOpintopisteissaTaiKursseissa'

/**
 * Valtakunnallisen lukioon valmistavan koulutuksen kurssin tai moduulin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.ValtakunnallinenLukioonValmistavanKoulutuksenKurssi`
 */
export type ValtakunnallinenLukioonValmistavanKoulutuksenKurssi = {
  $class: 'fi.oph.koski.schema.ValtakunnallinenLukioonValmistavanKoulutuksenKurssi'
  tunniste: Koodistokoodiviite<
    | 'lukioonvalmistavankoulutuksenkurssit2015'
    | 'lukioonvalmistavankoulutuksenmoduulit2019',
    string
  >
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
}

export const ValtakunnallinenLukioonValmistavanKoulutuksenKurssi = (o: {
  tunniste: Koodistokoodiviite<
    | 'lukioonvalmistavankoulutuksenkurssit2015'
    | 'lukioonvalmistavankoulutuksenmoduulit2019',
    string
  >
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
}): ValtakunnallinenLukioonValmistavanKoulutuksenKurssi => ({
  $class:
    'fi.oph.koski.schema.ValtakunnallinenLukioonValmistavanKoulutuksenKurssi',
  ...o
})

ValtakunnallinenLukioonValmistavanKoulutuksenKurssi.className =
  'fi.oph.koski.schema.ValtakunnallinenLukioonValmistavanKoulutuksenKurssi' as const

export const isValtakunnallinenLukioonValmistavanKoulutuksenKurssi = (
  a: any
): a is ValtakunnallinenLukioonValmistavanKoulutuksenKurssi =>
  a?.$class ===
  'fi.oph.koski.schema.ValtakunnallinenLukioonValmistavanKoulutuksenKurssi'
