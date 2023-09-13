import { Constraint } from './Constraint'

/**
 * OptionalConstraint
 *
 * @see `fi.oph.koski.typemodel.OptionalConstraint`
 */
export type OptionalConstraint = {
  $class: 'fi.oph.koski.typemodel.OptionalConstraint'
  infoLinkTitle?: string
  infoLinkUrl?: string
  optional: Constraint
  default?: any
  infoDescription?: string
  type: 'optional'
}

export const OptionalConstraint = (o: {
  infoLinkTitle?: string
  infoLinkUrl?: string
  optional: Constraint
  default?: any
  infoDescription?: string
  type?: 'optional'
}): OptionalConstraint => ({
  type: 'optional',
  $class: 'fi.oph.koski.typemodel.OptionalConstraint',
  ...o
})

OptionalConstraint.className =
  'fi.oph.koski.typemodel.OptionalConstraint' as const

export const isOptionalConstraint = (a: any): a is OptionalConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.OptionalConstraint'
