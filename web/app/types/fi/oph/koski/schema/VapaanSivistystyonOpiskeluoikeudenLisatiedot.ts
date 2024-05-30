import { Maksuttomuus } from './Maksuttomuus'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * VapaanSivistystyönOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeudenLisätiedot`
 */
export type VapaanSivistystyönOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeudenLisätiedot'
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero', string>
}

export const VapaanSivistystyönOpiskeluoikeudenLisätiedot = (
  o: {
    maksuttomuus?: Array<Maksuttomuus>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero', string>
  } = {}
): VapaanSivistystyönOpiskeluoikeudenLisätiedot => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeudenLisätiedot',
  ...o
})

VapaanSivistystyönOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeudenLisätiedot' as const

export const isVapaanSivistystyönOpiskeluoikeudenLisätiedot = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeudenLisätiedot'
