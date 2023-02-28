import { YlioppilastutkinnonTutkintokokonaisuudenLisätiedot } from './YlioppilastutkinnonTutkintokokonaisuudenLisatiedot'

/**
 * YlioppilastutkinnonOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenLisätiedot`
 */
export type YlioppilastutkinnonOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenLisätiedot'
  tutkintokokonaisuudet?: Array<YlioppilastutkinnonTutkintokokonaisuudenLisätiedot>
}

export const YlioppilastutkinnonOpiskeluoikeudenLisätiedot = (
  o: {
    tutkintokokonaisuudet?: Array<YlioppilastutkinnonTutkintokokonaisuudenLisätiedot>
  } = {}
): YlioppilastutkinnonOpiskeluoikeudenLisätiedot => ({
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenLisätiedot',
  ...o
})

YlioppilastutkinnonOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenLisätiedot' as const

export const isYlioppilastutkinnonOpiskeluoikeudenLisätiedot = (
  a: any
): a is YlioppilastutkinnonOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenLisätiedot'
