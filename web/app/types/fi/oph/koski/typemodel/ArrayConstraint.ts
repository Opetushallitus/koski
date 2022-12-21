import { Constraint } from './Constraint'

/**
 * ArrayConstraint
 *
 * @see `fi.oph.koski.typemodel.ArrayConstraint`
 */
export type ArrayConstraint = {
  $class: 'fi.oph.koski.typemodel.ArrayConstraint'
  items: Constraint
  maxItems?: number
  default?: any
  minItems?: number
  type: 'array'
}

export const ArrayConstraint = (o: {
  items: Constraint
  maxItems?: number
  default?: any
  minItems?: number
  type?: 'array'
}): ArrayConstraint => ({
  type: 'array',
  $class: 'fi.oph.koski.typemodel.ArrayConstraint',
  ...o
})

export const isArrayConstraint = (a: any): a is ArrayConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.ArrayConstraint'
