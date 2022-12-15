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

export const isPerusopetukseenValmistavanOpetuksenOppiaine = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOppiaine =>
  a?.$class === 'PerusopetukseenValmistavanOpetuksenOppiaine'
