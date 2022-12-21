import { Constraint } from './Constraint'

/**
 * UnionConstraint
 *
 * @see `fi.oph.koski.typemodel.UnionConstraint`
 */
export type UnionConstraint = {
  $class: 'fi.oph.koski.typemodel.UnionConstraint'
  anyOf: Record<string, Constraint>
  type: 'union'
}

export const UnionConstraint = (
  o: {
    anyOf?: Record<string, Constraint>
    type?: 'union'
  } = {}
): UnionConstraint => ({
  $class: 'fi.oph.koski.typemodel.UnionConstraint',
  anyOf: {},
  type: 'union',
  ...o
})

export const isUnionConstraint = (a: any): a is UnionConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.UnionConstraint'
