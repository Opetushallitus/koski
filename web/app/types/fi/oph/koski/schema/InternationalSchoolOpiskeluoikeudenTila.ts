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

InternationalSchoolOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenTila' as const

export const isInternationalSchoolOpiskeluoikeudenTila = (
  a: any
): a is InternationalSchoolOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenTila'
