import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'
import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenUskonto`
 */
export type NuortenPerusopetuksenUskonto = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenUskonto'
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}

export const NuortenPerusopetuksenUskonto = (o: {
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}): NuortenPerusopetuksenUskonto => ({
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenUskonto',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'KT',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

NuortenPerusopetuksenUskonto.className =
  'fi.oph.koski.schema.NuortenPerusopetuksenUskonto' as const

export const isNuortenPerusopetuksenUskonto = (
  a: any
): a is NuortenPerusopetuksenUskonto =>
  a?.$class === 'fi.oph.koski.schema.NuortenPerusopetuksenUskonto'
