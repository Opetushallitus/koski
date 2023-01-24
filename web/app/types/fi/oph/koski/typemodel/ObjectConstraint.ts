import { Constraint } from './Constraint'

/**
 * ObjectConstraint
 *
 * @see `fi.oph.koski.typemodel.ObjectConstraint`
 */
export type ObjectConstraint = {
  $class: 'fi.oph.koski.typemodel.ObjectConstraint'
  default?: object
  class: string
  properties: Record<string, Constraint>
  type: 'object'
}

export const ObjectConstraint = (o: {
  default?: object
  class: string
  properties?: Record<string, Constraint>
  type?: 'object'
}): ObjectConstraint => ({
  $class: 'fi.oph.koski.typemodel.ObjectConstraint',
  properties: {},
  type: 'object',
  ...o
})

ObjectConstraint.className = 'fi.oph.koski.typemodel.ObjectConstraint' as const

export const isObjectConstraint = (a: any): a is ObjectConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.ObjectConstraint'
