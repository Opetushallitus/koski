import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'
import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenMuuOppiaine`
 */
export type AhvenanmaanPerusopetuksenMuuOppiaine = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenMuuOppiaine'
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'HI'
    | 'MU'
    | 'BI'
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
    | 'PS'
  >
}

export const AhvenanmaanPerusopetuksenMuuOppiaine = (o: {
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'HI'
    | 'MU'
    | 'BI'
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
    | 'PS'
  >
}): AhvenanmaanPerusopetuksenMuuOppiaine => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenMuuOppiaine',
  ...o
})

AhvenanmaanPerusopetuksenMuuOppiaine.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenMuuOppiaine' as const

export const isAhvenanmaanPerusopetuksenMuuOppiaine = (
  a: any
): a is AhvenanmaanPerusopetuksenMuuOppiaine =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenMuuOppiaine'
