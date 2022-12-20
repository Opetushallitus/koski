import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenUskonto`
 */
export type NuortenPerusopetuksenUskonto = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenUskonto'
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export const NuortenPerusopetuksenUskonto = (o: {
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}): NuortenPerusopetuksenUskonto => ({
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenUskonto',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'KT',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

export const isNuortenPerusopetuksenUskonto = (
  a: any
): a is NuortenPerusopetuksenUskonto =>
  a?.$class === 'fi.oph.koski.schema.NuortenPerusopetuksenUskonto'
