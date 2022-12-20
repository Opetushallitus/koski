import { TutkintokoulutukseenValmentavanOpiskeluoikeusjakso } from './TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'

/**
 * TutkintokoulutukseenValmentavanOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenTila`
 */
export type TutkintokoulutukseenValmentavanOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<TutkintokoulutukseenValmentavanOpiskeluoikeusjakso>
}

export const TutkintokoulutukseenValmentavanOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<TutkintokoulutukseenValmentavanOpiskeluoikeusjakso>
  } = {}
): TutkintokoulutukseenValmentavanOpiskeluoikeudenTila => ({
  $class:
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenTila = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenTila =>
  a?.$class ===
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
