import * as A from 'fp-ts/Array'
import { isArrayConstraint } from '../types/fi/oph/koski/typemodel/ArrayConstraint'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { isObjectConstraint } from '../types/fi/oph/koski/typemodel/ObjectConstraint'
import { isStringConstraint } from '../types/fi/oph/koski/typemodel/StringConstraint'
import { ClassOf, ObjWithClass } from './types'

export const path =
  (pathStr: string) =>
  (constraint: Constraint | null): Constraint | null =>
    constraint
      ? pathStr
          .split('.')
          .map((key) => (key === '[]' ? elems : prop(key)))
          .reduce<Constraint | null>((acc, f) => f(acc), constraint)
      : null

export const hasProp = (
  constraint: Constraint | null,
  propKey: string
): boolean =>
  isObjectConstraint(constraint) && constraint.properties[propKey] !== undefined

export const prop =
  (...propNamePath: string[]) =>
  (constraint: Constraint | null): Constraint | null => {
    if (A.isEmpty(propNamePath)) {
      return constraint
    }
    const c =
      (isObjectConstraint(constraint) &&
        constraint.properties[propNamePath[0]]) ||
      null
    return c ? prop(...propNamePath.slice(1))(c) : null
  }

export const className =
  <T extends ObjWithClass>() =>
  (constraint: Constraint | null): ClassOf<T> | null =>
    isObjectConstraint(constraint) ? constraint.class : null

export const elems = (constraint: Constraint | null): Constraint | null =>
  isArrayConstraint(constraint) ? constraint.items : null

export const allowedStrings = (
  constraint: Constraint | null
): string[] | null =>
  (isStringConstraint(constraint) && constraint.enum) || null

export type KoodiviiteConstraint = {
  koodistoUri: string | null
  koodiarvot: string[] | null
}

export const toKoodiviite = (
  constraint: Constraint | null
): KoodiviiteConstraint | null =>
  isObjectConstraint(constraint) &&
  constraint.class === 'fi.oph.koski.schema.Koodistokoodiviite'
    ? {
        koodistoUri:
          allowedStrings(prop('koodistoUri')(constraint))?.[0] || null,
        koodiarvot: allowedStrings(prop('koodiarvo')(constraint)) || null
      }
    : null
