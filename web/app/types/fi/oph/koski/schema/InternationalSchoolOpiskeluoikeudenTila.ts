import { InternationalSchoolOpiskeluoikeusjakso } from './InternationalSchoolOpiskeluoikeusjakso'

/**
 * InternationalSchoolOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenTila`
 */
export type InternationalSchoolOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<InternationalSchoolOpiskeluoikeusjakso>
}

export const InternationalSchoolOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<InternationalSchoolOpiskeluoikeusjakso>
  } = {}
): InternationalSchoolOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

export const isInternationalSchoolOpiskeluoikeudenTila = (
  a: any
): a is InternationalSchoolOpiskeluoikeudenTila =>
  a?.$class === 'InternationalSchoolOpiskeluoikeudenTila'
