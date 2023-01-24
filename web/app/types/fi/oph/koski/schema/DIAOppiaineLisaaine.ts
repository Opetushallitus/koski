import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * DIA-oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAOppiaineLisäaine`
 */
export type DIAOppiaineLisäaine = {
  $class: 'fi.oph.koski.schema.DIAOppiaineLisäaine'
  tunniste: Koodistokoodiviite<
    'oppiaineetdia',
    | 'CLOE'
    | 'CCEA'
    | 'LT'
    | 'MASY'
    | 'MALI'
    | 'LI'
    | 'VELI'
    | 'ELI'
    | 'RALI'
    | 'VT'
  >
  laajuus?: LaajuusVuosiviikkotunneissa
}

export const DIAOppiaineLisäaine = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetdia',
    | 'CLOE'
    | 'CCEA'
    | 'LT'
    | 'MASY'
    | 'MALI'
    | 'LI'
    | 'VELI'
    | 'ELI'
    | 'RALI'
    | 'VT'
  >
  laajuus?: LaajuusVuosiviikkotunneissa
}): DIAOppiaineLisäaine => ({
  $class: 'fi.oph.koski.schema.DIAOppiaineLisäaine',
  ...o
})

DIAOppiaineLisäaine.className =
  'fi.oph.koski.schema.DIAOppiaineLisäaine' as const

export const isDIAOppiaineLisäaine = (a: any): a is DIAOppiaineLisäaine =>
  a?.$class === 'fi.oph.koski.schema.DIAOppiaineLisäaine'
