/**
 * BooleanConstraint
 *
 * @see `fi.oph.koski.typemodel.BooleanConstraint`
 */
export type BooleanConstraint = {
  $class: 'fi.oph.koski.typemodel.BooleanConstraint'
  default?: any
  enum?: Array<boolean>
  type: 'bool'
}

export const BooleanConstraint = (
  o: {
    default?: any
    enum?: Array<boolean>
    type?: 'bool'
  } = {}
): BooleanConstraint => ({
  $class: 'fi.oph.koski.typemodel.BooleanConstraint',
  type: 'bool',
  ...o
})

export const isBooleanConstraint = (a: any): a is BooleanConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.BooleanConstraint'
