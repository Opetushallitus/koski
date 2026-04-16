import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * Ahvenanmaan perusopetuksen toiminta-alueen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlue`
 */
export type AhvenanmaanPerusopetuksenToimintaAlue = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlue'
  tunniste: Koodistokoodiviite<'perusopetuksentoimintaalue', string>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export const AhvenanmaanPerusopetuksenToimintaAlue = (o: {
  tunniste: Koodistokoodiviite<'perusopetuksentoimintaalue', string>
  laajuus?: LaajuusVuosiviikkotunneissa
}): AhvenanmaanPerusopetuksenToimintaAlue => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlue',
  ...o
})

AhvenanmaanPerusopetuksenToimintaAlue.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlue' as const

export const isAhvenanmaanPerusopetuksenToimintaAlue = (
  a: any
): a is AhvenanmaanPerusopetuksenToimintaAlue =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlue'
