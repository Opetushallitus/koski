import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusKaikkiYksiköt } from './LaajuusKaikkiYksikot'
import { LocalizedString } from './LocalizedString'

/**
 * Perusopetukseen valmistavan opetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaine`
 */
export type PerusopetukseenValmistavanOpetuksenOppiaine = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaine'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  opetuksenSisältö?: LocalizedString
}

export const PerusopetukseenValmistavanOpetuksenOppiaine = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  opetuksenSisältö?: LocalizedString
}): PerusopetukseenValmistavanOpetuksenOppiaine => ({
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaine',
  ...o
})

PerusopetukseenValmistavanOpetuksenOppiaine.className =
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaine' as const

export const isPerusopetukseenValmistavanOpetuksenOppiaine = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOppiaine =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaine'
