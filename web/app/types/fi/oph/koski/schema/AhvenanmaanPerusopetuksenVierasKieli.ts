import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenVierasKieli`
 */
export type AhvenanmaanPerusopetuksenVierasKieli = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVierasKieli'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
}

export const AhvenanmaanPerusopetuksenVierasKieli = (o: {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
}): AhvenanmaanPerusopetuksenVierasKieli => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVierasKieli',
  ...o
})

AhvenanmaanPerusopetuksenVierasKieli.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVierasKieli' as const

export const isAhvenanmaanPerusopetuksenVierasKieli = (
  a: any
): a is AhvenanmaanPerusopetuksenVierasKieli =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVierasKieli'
