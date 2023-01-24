import { Constraint } from './Constraint'

/**
 * OptionalConstraint
 *
 * @see `fi.oph.koski.typemodel.OptionalConstraint`
 */
export type OptionalConstraint = {
  $class: 'fi.oph.koski.typemodel.OptionalConstraint'
  default?: any
  optional: Constraint
  type: 'optional'
}

export const OptionalConstraint = (o: {
  default?: any
  optional: Constraint
  type?: 'optional'
}): OptionalConstraint => ({
  $class: 'fi.oph.koski.typemodel.OptionalConstraint',
  type: 'optional',
  ...o
})

OptionalConstraint.className =
  'fi.oph.koski.typemodel.OptionalConstraint' as const

export const isOptionalConstraint = (a: any): a is OptionalConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.OptionalConstraint'
