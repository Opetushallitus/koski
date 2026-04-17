import { Aikajakso } from './Aikajakso'

/**
 * AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot`
 */
export type AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot'
  kotiopetusjaksot?: Array<Aikajakso>
  tavoitekokonaisuuksittainOpiskelu?: Array<Aikajakso>
  vuosiluokkiinSitoutumatonOpetus?: boolean
}

export const AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    kotiopetusjaksot?: Array<Aikajakso>
    tavoitekokonaisuuksittainOpiskelu?: Array<Aikajakso>
    vuosiluokkiinSitoutumatonOpetus?: boolean
  } = {}
): AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot',
  ...o
})

AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot' as const

export const isAhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot'
