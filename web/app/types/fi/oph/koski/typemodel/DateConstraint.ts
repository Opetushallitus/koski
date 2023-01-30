/**
 * DateConstraint
 *
 * @see `fi.oph.koski.typemodel.DateConstraint`
 */
export type DateConstraint = {
  $class: 'fi.oph.koski.typemodel.DateConstraint'
  type: 'date'
}

export const DateConstraint = (
  o: {
    type?: 'date'
  } = {}
): DateConstraint => ({
  $class: 'fi.oph.koski.typemodel.DateConstraint',
  type: 'date',
  ...o
})

DateConstraint.className = 'fi.oph.koski.typemodel.DateConstraint' as const

export const isDateConstraint = (a: any): a is DateConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.DateConstraint'
