import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * PaikallinenLukioonValmistavanKoulutuksenOppiaine
 *
 * @see `fi.oph.koski.schema.PaikallinenLukioonValmistavanKoulutuksenOppiaine`
 */
export type PaikallinenLukioonValmistavanKoulutuksenOppiaine = {
  $class: 'fi.oph.koski.schema.PaikallinenLukioonValmistavanKoulutuksenOppiaine'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export const PaikallinenLukioonValmistavanKoulutuksenOppiaine = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}): PaikallinenLukioonValmistavanKoulutuksenOppiaine => ({
  $class:
    'fi.oph.koski.schema.PaikallinenLukioonValmistavanKoulutuksenOppiaine',
  ...o
})

export const isPaikallinenLukioonValmistavanKoulutuksenOppiaine = (
  a: any
): a is PaikallinenLukioonValmistavanKoulutuksenOppiaine =>
  a?.$class === 'PaikallinenLukioonValmistavanKoulutuksenOppiaine'
