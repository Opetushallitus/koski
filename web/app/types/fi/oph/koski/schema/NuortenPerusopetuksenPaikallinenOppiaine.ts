import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'
import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenPaikallinenOppiaine`
 */
export type NuortenPerusopetuksenPaikallinenOppiaine = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenPaikallinenOppiaine'
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export const NuortenPerusopetuksenPaikallinenOppiaine = (o: {
  pakollinen?: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}): NuortenPerusopetuksenPaikallinenOppiaine => ({
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenPaikallinenOppiaine',
  pakollinen: false,
  ...o
})

export const isNuortenPerusopetuksenPaikallinenOppiaine = (
  a: any
): a is NuortenPerusopetuksenPaikallinenOppiaine =>
  a?.$class === 'fi.oph.koski.schema.NuortenPerusopetuksenPaikallinenOppiaine'
