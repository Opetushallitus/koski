/**
 * RefConstraint
 *
 * @see `fi.oph.koski.typemodel.RefConstraint`
 */
export type RefConstraint = {
  $class: 'fi.oph.koski.typemodel.RefConstraint'
  type: 'objectRef'
  className: string
}

export const RefConstraint = (o: {
  type?: 'objectRef'
  className: string
}): RefConstraint => ({
  $class: 'fi.oph.koski.typemodel.RefConstraint',
  type: 'objectRef',
  ...o
})

RefConstraint.className = 'fi.oph.koski.typemodel.RefConstraint' as const

export const isRefConstraint = (a: any): a is RefConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.RefConstraint'
