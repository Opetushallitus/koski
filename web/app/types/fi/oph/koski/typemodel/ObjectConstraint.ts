import { Constraint } from './Constraint'

/**
 * ObjectConstraint
 *
 * @see `fi.oph.koski.typemodel.ObjectConstraint`
 */
export type ObjectConstraint = {
  $class: 'fi.oph.koski.typemodel.ObjectConstraint'
  infoLinkTitle?: string
  infoLinkUrl?: string
  properties: Record<string, Constraint>
  default?: object
  infoDescription?: string
  class: string
  type: 'object'
}

export const ObjectConstraint = (o: {
  infoLinkTitle?: string
  infoLinkUrl?: string
  properties?: Record<string, Constraint>
  default?: object
  infoDescription?: string
  class: string
  type?: 'object'
}): ObjectConstraint => ({
  properties: {},
  type: 'object',
  $class: 'fi.oph.koski.typemodel.ObjectConstraint',
  ...o
})

ObjectConstraint.className = 'fi.oph.koski.typemodel.ObjectConstraint' as const

export const isObjectConstraint = (a: any): a is ObjectConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.ObjectConstraint'
