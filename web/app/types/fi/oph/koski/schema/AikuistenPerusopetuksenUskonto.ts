import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenUskonto`
 */
export type AikuistenPerusopetuksenUskonto = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenUskonto'
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export const AikuistenPerusopetuksenUskonto = (o: {
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}): AikuistenPerusopetuksenUskonto => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenUskonto',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'KT',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

export const isAikuistenPerusopetuksenUskonto = (
  a: any
): a is AikuistenPerusopetuksenUskonto =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenUskonto'
