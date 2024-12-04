import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { ItemOf } from '../../util/types'

export const tunnisteGuard =
  <U extends string, T extends readonly string[]>(uri: U, koodiarvot: T) =>
  (k?: Koodistokoodiviite): k is Koodistokoodiviite<U, ItemOf<T>> =>
    !!k && k.koodistoUri === uri && koodiarvot.includes(k.koodiarvo)

export const tunnisteUriGuard =
  <U extends string>(uri: U) =>
  (k?: Koodistokoodiviite): k is Koodistokoodiviite<U> =>
    !!k && k.koodistoUri === uri

export const isIBOppiaineLanguageTunniste = tunnisteGuard('oppiaineetib', [
  'A',
  'A2',
  'B',
  'AB',
  'CLA'
] as const)

export const isIBOppiaineMuuTunniste = tunnisteGuard('oppiaineetib', [
  'BIO',
  'BU',
  'CHE',
  'DAN',
  'DIS',
  'ECO',
  'FIL',
  'GEO',
  'HIS',
  'MAT',
  'MATFT',
  'MATST',
  'MUS',
  'PHI',
  'PHY',
  'POL',
  'PSY',
  'REL',
  'SOC',
  'ESS',
  'THE',
  'VA',
  'CS',
  'LIT',
  'INF',
  'DES',
  'SPO',
  'MATAA',
  'MATAI'
] as const)

export const isLukionMatematiikka2015Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['MA']
)

export const isLukionMuuValtakunnallinenOppiaine2015Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  [
    'HI',
    'MU',
    'BI',
    'PS',
    'ET',
    'KO',
    'FI',
    'KE',
    'YH',
    'TE',
    'KS',
    'FY',
    'GE',
    'LI',
    'KU',
    'OP'
  ] as const
)

export const isLukionUskonto2015Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['KT'] as const
)

export const isLukionÄidinkieliJaKirjallisuus2015Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['AI'] as const
)

export const isVierasTaiToinenKotimainenKieli2015Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['A1', 'A2', 'B1', 'B2', 'B3', 'AOM'] as const
)

export const isLukionMatematiikka2019Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['MA'] as const
)

export const isLukionMuuValtakunnallinenOppiaine2019Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  [
    'HI',
    'MU',
    'BI',
    'PS',
    'ET',
    'FI',
    'KE',
    'YH',
    'TE',
    'FY',
    'GE',
    'LI',
    'KU',
    'OP'
  ] as const
)

export const isLukionUskonto2019Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['KT'] as const
)

export const isLukionÄidinkieliJaKirjallisuus2019Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['AI'] as const
)

export const isVierasTaiToinenKotimainenKieli2019Tunniste = tunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['A', 'B1', 'B2', 'B3', 'AOM'] as const
)

export const isLukiodiplomit2019Tunniste = tunnisteGuard('lukionmuutopinnot', [
  'LD'
] as const)

export const isMuutLukionSuoritukset2019Tunniste = tunnisteGuard(
  'lukionmuutopinnot',
  ['MS'] as const
)

export const isTemaattisetOpinnot2019Tunniste = tunnisteGuard(
  'lukionmuutopinnot',
  ['TO'] as const
)
