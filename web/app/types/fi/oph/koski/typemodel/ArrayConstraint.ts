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
  type: 'array'
  default?: any
  minItems?: number
}

export const ArrayConstraint = (o: {
  items: Constraint
  maxItems?: number
  type?: 'array'
  default?: any
  minItems?: number
}): ArrayConstraint => ({
  type: 'array',
  $class: 'fi.oph.koski.typemodel.ArrayConstraint',
  ...o
})

ArrayConstraint.className = 'fi.oph.koski.typemodel.ArrayConstraint' as const

export const isArrayConstraint = (a: any): a is ArrayConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.ArrayConstraint'
