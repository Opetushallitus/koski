/**
 * AnyConstraint
 *
 * @see `fi.oph.koski.typemodel.AnyConstraint`
 */
export type AnyConstraint = {
  $class: 'fi.oph.koski.typemodel.AnyConstraint'
  type: 'any'
}

export const AnyConstraint = (
  o: {
    type?: 'any'
  } = {}
): AnyConstraint => ({
  $class: 'fi.oph.koski.typemodel.AnyConstraint',
  type: 'any',
  ...o
})

export const isAnyConstraint = (a: any): a is AnyConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.AnyConstraint'
