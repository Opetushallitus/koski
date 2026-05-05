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
    | 'YL'
    | 'BI'
    | 'GE'
    | 'FK'
    | 'FY'
    | 'KE'
    | 'TE'
    | 'RO'
    | 'HI'
    | 'MU'
    | 'YH'
    | 'KU'
    | 'KS'
    | 'TKS'
    | 'TES'
    | 'LI'
    | 'KO'
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
    | 'YL'
    | 'BI'
    | 'GE'
    | 'FK'
    | 'FY'
    | 'KE'
    | 'TE'
    | 'RO'
    | 'HI'
    | 'MU'
    | 'YH'
    | 'KU'
    | 'KS'
    | 'TKS'
    | 'TES'
    | 'LI'
    | 'KO'
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
