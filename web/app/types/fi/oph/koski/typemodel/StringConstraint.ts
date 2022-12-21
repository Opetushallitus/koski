/**
 * StringConstraint
 *
 * @see `fi.oph.koski.typemodel.StringConstraint`
 */
export type StringConstraint = {
  $class: 'fi.oph.koski.typemodel.StringConstraint'
  default?: any
  enum?: Array<string>
  type: 'string'
}

export const StringConstraint = (
  o: {
    default?: any
    enum?: Array<string>
    type?: 'string'
  } = {}
): StringConstraint => ({
  $class: 'fi.oph.koski.typemodel.StringConstraint',
  type: 'string',
  ...o
})

export const isStringConstraint = (a: any): a is StringConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.StringConstraint'
