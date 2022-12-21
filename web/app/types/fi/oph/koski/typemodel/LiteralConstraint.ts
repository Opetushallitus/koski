/**
 * LiteralConstraint
 *
 * @see `fi.oph.koski.typemodel.LiteralConstraint`
 */
export type LiteralConstraint = {
  $class: 'fi.oph.koski.typemodel.LiteralConstraint'
  constant: string
  type: 'string'
}

export const LiteralConstraint = (o: {
  constant: string
  type?: 'string'
}): LiteralConstraint => ({
  $class: 'fi.oph.koski.typemodel.LiteralConstraint',
  type: 'string',
  ...o
})

export const isLiteralConstraint = (a: any): a is LiteralConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.LiteralConstraint'
