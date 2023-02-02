import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import * as O from 'fp-ts/Option'
import {
  isLocalizedString,
  LocalizedString
} from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  ArrayConstraint,
  isArrayConstraint
} from '../../types/fi/oph/koski/typemodel/ArrayConstraint'
import { Constraint } from '../../types/fi/oph/koski/typemodel/Constraint'
import {
  isNumberConstraint,
  NumberConstraint
} from '../../types/fi/oph/koski/typemodel/NumberConstraint'
import {
  isObjectConstraint,
  ObjectConstraint
} from '../../types/fi/oph/koski/typemodel/ObjectConstraint'
import {
  isOptionalConstraint,
  OptionalConstraint
} from '../../types/fi/oph/koski/typemodel/OptionalConstraint'
import {
  isStringConstraint,
  StringConstraint
} from '../../types/fi/oph/koski/typemodel/StringConstraint'
import {
  isUnionConstraint,
  UnionConstraint
} from '../../types/fi/oph/koski/typemodel/UnionConstraint'
import { nonFalsy } from '../../util/fp/arrays'

export type ValidationError =
  | InvalidTypeError
  | EmptyStringError
  | InvalidDateError
  | MustBeGreaterThanError
  | MustBeAtLeastError
  | MustBeLesserThanError
  | MustBeAtMostError
  | NoMatchError
  | NoClassNameError

export const isValidationError = (a: any): a is ValidationError =>
  typeof a === 'object' && typeof a.type === 'string'

export type InvalidTypeError = {
  type: 'invalidType'
  expected: string
  actual: any
  path: string
}

export type EmptyStringError = {
  type: 'emptyString'
  path: string
}

export type InvalidDateError = {
  type: 'invalidDate'
  actual: string
  path: string
}

export type MustBeGreaterThanError = {
  type: 'mustBeGreater'
  limit: number
  actual: number
  path: string
}

export type MustBeAtLeastError = {
  type: 'mustBeAtLeast'
  limit: number
  actual: number
  path: string
}

export type MustBeLesserThanError = {
  type: 'mustBeLesser'
  limit: number
  actual: number
  path: string
}

export type MustBeAtMostError = {
  type: 'mustBeAtMost'
  limit: number
  actual: number
  path: string
}

export type NoMatchError = {
  type: 'noMatch'
  expected: string[]
  actual: any
  data: any
  path: string
}

export type NoClassNameError = {
  type: 'noClassName'
  data: any
  path: string
}

export const validateData = (
  data: unknown,
  constraint: Constraint
): ValidationError[] => validate(data, constraint, [])

const validate = (
  data: unknown,
  constraint: Constraint,
  path: string[]
): ValidationError[] => {
  if (isLocalizedString(data)) {
    return validateLocalizationString(data, path)
  } else if (isObjectConstraint(constraint)) {
    return validateObject(data, constraint, path)
  } else if (isArrayConstraint(constraint)) {
    return validateArray(data, constraint, path)
  } else if (isUnionConstraint(constraint)) {
    return validateUnion(data, constraint, path)
  } else if (isOptionalConstraint(constraint)) {
    return validateOptional(data, constraint, path)
  } else if (isNumberConstraint(constraint)) {
    return validateNumber(data, constraint, path)
  } else if (isStringConstraint(constraint)) {
    return validateString(data, constraint, path)
  }
  return []
}

// ObjectConstraint
const validateObject = (
  data: unknown,
  constraint: ObjectConstraint,
  path: string[]
): ValidationError[] => {
  if (typeof data !== 'object') {
    return [invalidType('object', data, path)]
  } else if (data === null || data === undefined) {
    return [invalidType('object', data, path)]
  } else if ((data as any).$class === undefined) {
    return [noClassName(data, path)]
  } else {
    return Object.entries(constraint.properties)
      .map(([key, child]) =>
        validate((data as any)[key], child, [...path, key])
      )
      .flat()
  }
}

// ArrayConstraint
const validateArray = (
  data: unknown,
  constraint: ArrayConstraint,
  path: string[]
): ValidationError[] => {
  if (!Array.isArray(data)) {
    return [invalidType('array', data, path)]
  } else {
    return data.flatMap((e, i) =>
      validate(e, constraint.items, [...path, i.toString()])
    )
  }
}

// UnionConstraint
const validateUnion = (
  data: unknown,
  constraint: UnionConstraint,
  path: string[]
): ValidationError[] => {
  const className = (data as any)?.$class as string
  if (!className) {
    return [noMatch(Object.keys(constraint.anyOf), className, data, path)]
  }
  const childC = constraint.anyOf[className]
  if (!childC) {
    return [noMatch(Object.keys(constraint.anyOf), className, data, path)]
  }
  return validate(data, childC, path)
}

// OptionalConstraint
const validateOptional = (
  data: unknown,
  constraint: OptionalConstraint,
  path: string[]
): ValidationError[] => {
  if (data === null || data === undefined) {
    return []
  }
  return validate(data, constraint.optional, path)
}

// NumberConstraint
const validateNumber = (
  data: unknown,
  constraint: NumberConstraint,
  path: string[]
): ValidationError[] => {
  if (typeof data === 'number') {
    return [
      constraint.min &&
        constraint.min.inclusive &&
        data < constraint.min.n &&
        mustBeAtLeast(constraint.min.n, data, path),
      constraint.min &&
        !constraint.min.inclusive &&
        data <= constraint.min.n &&
        mustBeGreater(constraint.min.n, data, path),
      constraint.max &&
        constraint.max.inclusive &&
        data > constraint.max.n &&
        mustBeAtMost(constraint.max.n, data, path),
      constraint.max &&
        !constraint.max.inclusive &&
        data >= constraint.max.n &&
        mustBeLesser(constraint.max.n, data, path)
    ].filter(nonFalsy)
  }
  return [invalidType('number', data, path)]
}

// | StringConstraint
const validateString = (
  data: unknown,
  constraint: StringConstraint,
  path: string[]
): ValidationError[] => {
  if (typeof data === 'string') {
    return [data.length === 0 && emptyString(path)].filter(nonFalsy)
  }
  return [invalidType('string', data, path)]
}

// TODO: Implement rest of the constraints
// | AnyConstraint
// | BooleanConstraint
// | DateConstraint
// | LiteralConstraint
// | RecordConstraint

// LocalizationString
const validateLocalizationString = (
  str: LocalizedString,
  path: string[]
): ValidationError[] =>
  (str as any).fi || (str as any).sv || str.en ? [] : [emptyString(path)]

// Error builders

const pathToString = (path: string[]) => path.join('.')

const invalidType = (
  expected: string,
  actual: any,
  path: string[]
): InvalidTypeError => {
  console.error(
    `Invalid type detected on form data at '${path}'. Expected ${expected} but got:`,
    actual
  )
  return {
    type: 'invalidType',
    expected,
    actual,
    path: pathToString(path)
  }
}

export const emptyString = (path: string[]): EmptyStringError => ({
  type: 'emptyString',
  path: pathToString(path)
})

export const invalidDate = (
  sample: string,
  path: string[]
): InvalidDateError => ({
  type: 'invalidDate',
  actual: sample,
  path: pathToString(path)
})

export const mustBeGreater = (
  limit: number,
  actual: number,
  path: string[]
): MustBeGreaterThanError => ({
  type: 'mustBeGreater',
  limit,
  actual,
  path: pathToString(path)
})

export const mustBeAtLeast = (
  limit: number,
  actual: number,
  path: string[]
): MustBeAtLeastError => ({
  type: 'mustBeAtLeast',
  limit,
  actual,
  path: pathToString(path)
})

export const mustBeLesser = (
  limit: number,
  actual: number,
  path: string[]
): MustBeLesserThanError => ({
  type: 'mustBeLesser',
  limit,
  actual,
  path: pathToString(path)
})

export const mustBeAtMost = (
  limit: number,
  actual: number,
  path: string[]
): MustBeAtMostError => ({
  type: 'mustBeAtMost',
  limit,
  actual,
  path: pathToString(path)
})

export const noMatch = (
  expected: string[],
  actual: any,
  data: any,
  path: string[]
): NoMatchError => ({
  type: 'noMatch',
  expected,
  actual,
  data,
  path: pathToString(path)
})

export const noClassName = (data: any, path: string[]): NoClassNameError => ({
  type: 'noClassName',
  data,
  path: pathToString(path)
})

// Utils

export const errorPathIs =
  (fn: (path: string) => boolean) => (error: ValidationError) =>
    fn(error.path)

export const narrowErrorsToLeaf =
  (pathPostfix: string) =>
  (
    errors?: ValidationError[]
  ): NEA.NonEmptyArray<ValidationError> | undefined =>
    pipe(
      O.fromNullable(errors),
      O.map(A.filter(errorPathIs((path) => path.endsWith(pathPostfix)))),
      O.chain(NEA.fromArray),
      O.toUndefined
    )