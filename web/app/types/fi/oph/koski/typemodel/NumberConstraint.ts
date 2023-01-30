import { Limit } from './Limit'

/**
 * NumberConstraint
 *
 * @see `fi.oph.koski.typemodel.NumberConstraint`
 */
export type NumberConstraint = {
  $class: 'fi.oph.koski.typemodel.NumberConstraint'
  min?: Limit
  max?: Limit
  enum?: Array<number>
  default?: any
  type: 'number'
  decimals?: number
}

export const NumberConstraint = (
  o: {
    min?: Limit
    max?: Limit
    enum?: Array<number>
    default?: any
    type?: 'number'
    decimals?: number
  } = {}
): NumberConstraint => ({
  type: 'number',
  $class: 'fi.oph.koski.typemodel.NumberConstraint',
  ...o
})

NumberConstraint.className = 'fi.oph.koski.typemodel.NumberConstraint' as const

export const isNumberConstraint = (a: any): a is NumberConstraint =>
  a?.$class === 'fi.oph.koski.typemodel.NumberConstraint'
