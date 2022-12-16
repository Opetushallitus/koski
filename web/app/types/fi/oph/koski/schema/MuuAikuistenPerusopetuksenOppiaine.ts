import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'
import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.MuuAikuistenPerusopetuksenOppiaine`
 */
export type MuuAikuistenPerusopetuksenOppiaine = {
  $class: 'fi.oph.koski.schema.MuuAikuistenPerusopetuksenOppiaine'
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'OPA'
    | 'HI'
    | 'MU'
    | 'BI'
    | 'PS'
    | 'ET'
    | 'KO'
    | 'FI'
    | 'KE'
    | 'YH'
    | 'TE'
    | 'KS'
    | 'FY'
    | 'GE'
    | 'LI'
    | 'KU'
    | 'MA'
    | 'YL'
    | 'OP'
  >
}

export const MuuAikuistenPerusopetuksenOppiaine = (o: {
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'OPA'
    | 'HI'
    | 'MU'
    | 'BI'
    | 'PS'
    | 'ET'
    | 'KO'
    | 'FI'
    | 'KE'
    | 'YH'
    | 'TE'
    | 'KS'
    | 'FY'
    | 'GE'
    | 'LI'
    | 'KU'
    | 'MA'
    | 'YL'
    | 'OP'
  >
}): MuuAikuistenPerusopetuksenOppiaine => ({
  $class: 'fi.oph.koski.schema.MuuAikuistenPerusopetuksenOppiaine',
  ...o
})

export const isMuuAikuistenPerusopetuksenOppiaine = (
  a: any
): a is MuuAikuistenPerusopetuksenOppiaine =>
  a?.$class === 'MuuAikuistenPerusopetuksenOppiaine'
