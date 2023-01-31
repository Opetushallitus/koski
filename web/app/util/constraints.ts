import * as A from 'fp-ts/Array'
import * as Eq from 'fp-ts/Eq'
import { pipe } from 'fp-ts/lib/function'
import { isArrayConstraint } from '../types/fi/oph/koski/typemodel/ArrayConstraint'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { isObjectConstraint } from '../types/fi/oph/koski/typemodel/ObjectConstraint'
import { isOptionalConstraint } from '../types/fi/oph/koski/typemodel/OptionalConstraint'
import { isStringConstraint } from '../types/fi/oph/koski/typemodel/StringConstraint'
import { isUnionConstraint } from '../types/fi/oph/koski/typemodel/UnionConstraint'
import { nonNull } from './fp/arrays'
import { ClassOf, ObjWithClass, schemaClassName, shortClassName } from './types'

export const asList = (c: Constraint | null): Constraint[] | null =>
  c ? [c] : null

export const singular = <T>(c: T[] | null): T | null => {
  if (!c) {
    return null
  }
  if (c.length === 0) {
    throw new Error('Tried to take an item from empty set')
  }
  if (c.length > 1) {
    throw new Error(
      `Tried to take a single item from a set of ${c.length} items: ${c
        .map((x) => `${x}`)
        .join(', ')}`
    )
  }
  return c[0]
}

export const flatMap =
  <T>(fn: (c: Constraint | null) => T[] | null) =>
  (constraints: Constraint[] | null): T[] | null => {
    const cs = constraints?.flatMap(fn) || null
    return !cs
      ? null
      : cs.every((c) => c === null)
      ? null
      : pipe(cs, A.filter(nonNull), A.uniq(Eq.eqStrict as Eq.Eq<T>))
  }

export const path =
  (pathStr: string) =>
  (constraints: Constraint[] | null): Constraint[] | null => {
    if (constraints) {
      const fns = pathStr
        .split('.')
        .map((key) => (key === '[]' ? elems : props(key)))
      return flatMap((constraint: Constraint | null): Constraint[] | null =>
        fns.reduce((acc, fn) => {
          try {
            return fn(acc)
          } catch (err) {
            throw new Error(
              `Invalid ${display(constraint)} path '${pathStr}': ${err}`
            )
          }
        }, asList(constraint))
      )(constraints)
    }
    return null
  }

export const hasProp = (
  constraint: Constraint | null,
  propKey: string
): boolean =>
  isObjectConstraint(constraint) && constraint.properties[propKey] !== undefined

export const prop =
  (...propNamePath: string[]) =>
  (constraint: Constraint | null): Constraint[] | null => {
    if (!constraint) {
      return constraint
    }
    if (isUnionConstraint(constraint)) {
      return props(...propNamePath)(Object.values(constraint.anyOf))
    }
    if (A.isEmpty(propNamePath)) {
      return asList(constraint)
    }
    if (isObjectConstraint(constraint)) {
      const [head, ...tail] = propNamePath
      const c = constraint.properties[head]
      if (!c) {
        throw new Error(
          `Property '${head}' does not exist in class ${shortClassName(
            constraint.class
          )}. Available properties: ${Object.keys(constraint.properties).join(
            ', '
          )}`
        )
      }
      return c ? prop(...tail)(c) : null
    }
    if (isOptionalConstraint(constraint)) {
      return prop(...propNamePath)(constraint.optional)
    }
    throw new Error(`${display(constraint)} cannot have any properties`)
  }

export const props = (...propNamePath: string[]) =>
  flatMap(prop(...propNamePath))

export const className =
  <T extends ObjWithClass>() =>
  (constraint: Constraint | null): ClassOf<T>[] | null => {
    if (!constraint) {
      return constraint
    }
    if (isObjectConstraint(constraint)) {
      return [constraint.class]
    }
    if (isOptionalConstraint(constraint)) {
      return className<T>()(constraint.optional)
    }
    if (isUnionConstraint(constraint)) {
      return Object.keys(constraint.anyOf)
    }
    throw new Error(`${display(constraint)} does not have a class name`)
  }

export const classNames = <T extends ObjWithClass>() => flatMap(className<T>())

export const elem = (constraint: Constraint | null): Constraint[] | null => {
  if (!constraint) {
    return constraint
  }
  if (isArrayConstraint(constraint)) {
    return [constraint.items]
  }
  if (isOptionalConstraint(constraint)) {
    return elem(constraint.optional)
  }
  throw new Error(`${display(constraint)} is not an array`)
}

export const elems = flatMap(elem)

export const allowedStrings = (
  constraint: Constraint | null
): string[] | null => {
  if (!constraint) {
    return null
  }
  if (isStringConstraint(constraint)) {
    return constraint.enum || []
  }
  if (isOptionalConstraint(constraint)) {
    return allowedStrings(constraint.optional)
  }
  throw new Error(`${display(constraint)} is not a string`)
}

export const allAllowedStrings = flatMap(allowedStrings)

export type KoodiviiteConstraint<T extends string> = {
  koodistoUri: T | null
  koodiarvot: string[] | null
}

export const koodiviite = <T extends string>(
  constraint: Constraint | null
): KoodiviiteConstraint<T> | null => {
  if (!constraint) {
    return null
  }
  if (
    isObjectConstraint(constraint) &&
    constraint.class === 'fi.oph.koski.schema.Koodistokoodiviite'
  ) {
    return {
      koodistoUri:
        (allowedStrings(singular(prop('koodistoUri')(constraint)))?.[0] as T) ||
        null,
      koodiarvot:
        allowedStrings(singular(prop('koodiarvo')(constraint))) || null
    }
  }
  throw new Error(`${display(constraint)} is not Object(Koodistokoodiviite)`)
}

export const display = (constraint: Constraint | null): string => {
  if (!constraint) {
    return 'null'
  }
  if (isObjectConstraint(constraint)) {
    return `Object<${shortClassName(constraint.class)}>`
  }
  if (isArrayConstraint(constraint)) {
    return `Array<${display(constraint.items)}>`
  }
  if (isOptionalConstraint(constraint)) {
    return `Optional<${display(constraint.optional)}>`
  }
  return shortClassName(constraint.$class)
}
