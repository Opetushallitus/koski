import { EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso } from './EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'

/**
 * EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila`
 */
export type EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso>
}

export const EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso>
  } = {}
): EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila' as const

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeudenTila = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila =>
  a?.$class ===
  'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila'
