import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'
import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.MuuNuortenPerusopetuksenOppiaine`
 */
export type MuuNuortenPerusopetuksenOppiaine = {
  $class: 'fi.oph.koski.schema.MuuNuortenPerusopetuksenOppiaine'
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
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

export const MuuNuortenPerusopetuksenOppiaine = (o: {
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
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
}): MuuNuortenPerusopetuksenOppiaine => ({
  $class: 'fi.oph.koski.schema.MuuNuortenPerusopetuksenOppiaine',
  ...o
})

MuuNuortenPerusopetuksenOppiaine.className =
  'fi.oph.koski.schema.MuuNuortenPerusopetuksenOppiaine' as const

export const isMuuNuortenPerusopetuksenOppiaine = (
  a: any
): a is MuuNuortenPerusopetuksenOppiaine =>
  a?.$class === 'fi.oph.koski.schema.MuuNuortenPerusopetuksenOppiaine'
