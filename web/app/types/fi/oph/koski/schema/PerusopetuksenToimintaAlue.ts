import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * Perusopetuksen toiminta-alueen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PerusopetuksenToiminta_Alue`
 */
export type PerusopetuksenToiminta_Alue = {
  $class: 'fi.oph.koski.schema.PerusopetuksenToiminta_Alue'
  tunniste: Koodistokoodiviite<'perusopetuksentoimintaalue', string>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export const PerusopetuksenToiminta_Alue = (o: {
  tunniste: Koodistokoodiviite<'perusopetuksentoimintaalue', string>
  laajuus?: LaajuusVuosiviikkotunneissa
}): PerusopetuksenToiminta_Alue => ({
  $class: 'fi.oph.koski.schema.PerusopetuksenToiminta_Alue',
  ...o
})

export const isPerusopetuksenToiminta_Alue = (
  a: any
): a is PerusopetuksenToiminta_Alue =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenToiminta_Alue'
