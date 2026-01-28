import { Aikajakso } from './Aikajakso'

/**
 * Perusopetukseen valmistavan opetuksen opiskeluoikeuden lisätiedot
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot`
 */
export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot'
  lisäopetus?: Array<Aikajakso>
}

export const PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    lisäopetus?: Array<Aikajakso>
  } = {}
): PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot',
  ...o
})

PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot' as const

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot'
