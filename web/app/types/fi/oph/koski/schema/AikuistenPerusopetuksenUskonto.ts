import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'
import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenUskonto`
 */
export type AikuistenPerusopetuksenUskonto = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenUskonto'
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}

export const AikuistenPerusopetuksenUskonto = (o: {
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}): AikuistenPerusopetuksenUskonto => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenUskonto',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'KT',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

AikuistenPerusopetuksenUskonto.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenUskonto' as const

export const isAikuistenPerusopetuksenUskonto = (
  a: any
): a is AikuistenPerusopetuksenUskonto =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenUskonto'
