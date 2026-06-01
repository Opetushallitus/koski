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
    'ahvenanmaankoskioppiaineetyleissivistava',
    | 'SV'
    | 'SVA'
    | 'MA'
    | 'OM'
    | 'BI'
    | 'GE'
    | 'FYKE'
    | 'FY'
    | 'KE'
    | 'TE'
    | 'RELI'
    | 'HI'
    | 'MU'
    | 'SA'
    | 'KU'
    | 'KS'
    | 'TX'
    | 'TN'
    | 'LI'
    | 'HEKO'
    | 'OP'
  >
}

export const AhvenanmaanPerusopetuksenMuuOppiaine = (o: {
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'ahvenanmaankoskioppiaineetyleissivistava',
    | 'SV'
    | 'SVA'
    | 'MA'
    | 'OM'
    | 'BI'
    | 'GE'
    | 'FYKE'
    | 'FY'
    | 'KE'
    | 'TE'
    | 'RELI'
    | 'HI'
    | 'MU'
    | 'SA'
    | 'KU'
    | 'KS'
    | 'TX'
    | 'TN'
    | 'LI'
    | 'HEKO'
    | 'OP'
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
