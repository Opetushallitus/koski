import { Ulkomaanjakso } from './Ulkomaanjakso'

/**
 * AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot`
 */
export type AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot'
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  koulutusvienti: boolean
  siirtynytUusiinTutkinnonPerusteisiin?: boolean
}

export const AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot = (
  o: {
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    koulutusvienti?: boolean
    siirtynytUusiinTutkinnonPerusteisiin?: boolean
  } = {}
): AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot',
  koulutusvienti: false,
  ...o
})

AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot' as const

export const isAhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot = (
  a: any
): a is AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot'
