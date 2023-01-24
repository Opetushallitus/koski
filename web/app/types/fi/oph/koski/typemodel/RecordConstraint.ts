import { Constraint } from './Constraint'

/**
 * RecordConstraint
 *
 * @see `fi.oph.koski.typemodel.RecordConstraint`
 */
export type RecordConstraint = {
  $class: 'fi.oph.koski.typemodel.RecordConstraint'
  default?: any
  items: Constraint
  type: 'object'
}

export const RecordConstraint = (o: {
  default?: any
  items: Constraint
  type?: 'object'
}): RecordConstraint => ({
  $class: 'fi.oph.koski.typemodel.RecordConstraint',
  type: 'object',
  ...o
})

RecordConstraint.className = 'fi.oph.koski.typemodel.RecordConstraint' as const

export const isRecordConstraint = (a: any): a is RecordConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.RecordConstraint'
