import { LocalizedString } from '../schema/LocalizedString'

/**
 * TutkinnonOsanOsaAlue
 *
 * @see `fi.oph.koski.tutkinto.TutkinnonOsanOsaAlue`
 */
export type TutkinnonOsanOsaAlue = {
  $class: 'fi.oph.koski.tutkinto.TutkinnonOsanOsaAlue'
  pakollisenOsanLaajuus?: number
  kieliKoodiarvo?: string
  koodiarvo: string
  valinnaisenOsanLaajuus?: number
  nimi: LocalizedString
  id: number
}

export const TutkinnonOsanOsaAlue = (o: {
  pakollisenOsanLaajuus?: number
  kieliKoodiarvo?: string
  koodiarvo: string
  valinnaisenOsanLaajuus?: number
  nimi: LocalizedString
  id: number
}): TutkinnonOsanOsaAlue => ({
  $class: 'fi.oph.koski.tutkinto.TutkinnonOsanOsaAlue',
  ...o
})

TutkinnonOsanOsaAlue.className =
  'fi.oph.koski.tutkinto.TutkinnonOsanOsaAlue' as const

export const isTutkinnonOsanOsaAlue = (a: any): a is TutkinnonOsanOsaAlue =>
  a?.$class === 'fi.oph.koski.tutkinto.TutkinnonOsanOsaAlue'
