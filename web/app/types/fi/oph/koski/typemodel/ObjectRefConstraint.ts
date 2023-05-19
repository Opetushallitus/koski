/**
 * ObjectRefConstraint
 *
 * @see `fi.oph.koski.typemodel.ObjectRefConstraint`
 */
export type ObjectRefConstraint = {
  $class: 'fi.oph.koski.typemodel.ObjectRefConstraint'
  class: string
  type: 'ref'
}

export const ObjectRefConstraint = (o: {
  class: string
  type?: 'ref'
}): ObjectRefConstraint => ({
  $class: 'fi.oph.koski.typemodel.ObjectRefConstraint',
  type: 'ref',
  ...o
})

ObjectRefConstraint.className =
  'fi.oph.koski.typemodel.ObjectRefConstraint' as const

export const isObjectRefConstraint = (a: any): a is ObjectRefConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.ObjectRefConstraint'
