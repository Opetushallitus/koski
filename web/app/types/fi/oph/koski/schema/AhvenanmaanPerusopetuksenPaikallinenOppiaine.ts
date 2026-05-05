import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'
import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenPaikallinenOppiaine`
 */
export type AhvenanmaanPerusopetuksenPaikallinenOppiaine = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenPaikallinenOppiaine'
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export const AhvenanmaanPerusopetuksenPaikallinenOppiaine = (o: {
  pakollinen?: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}): AhvenanmaanPerusopetuksenPaikallinenOppiaine => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenPaikallinenOppiaine',
  pakollinen: false,
  ...o
})

AhvenanmaanPerusopetuksenPaikallinenOppiaine.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenPaikallinenOppiaine' as const

export const isAhvenanmaanPerusopetuksenPaikallinenOppiaine = (
  a: any
): a is AhvenanmaanPerusopetuksenPaikallinenOppiaine =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenPaikallinenOppiaine'
