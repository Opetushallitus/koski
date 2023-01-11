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
  isUnionConstraint,
  UnionConstraint
} from '../../types/fi/oph/koski/typemodel/UnionConstraint'
import { nonFalsy } from '../../util/fp/arrays'

export type ValidationError =
  | InvalidTypeError
  | EmptyStringError
  | MustBeGreaterThanError
  | MustBeAtLeastError
  | MustBeLesserThanError
  | MustBeAtMostError
  | NoMatchError
  | NoClassNameError

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

// TODO: Implement rest of the constraints
// | AnyConstraint
// | BooleanConstraint
// | DateConstraint
// | LiteralConstraint
// | RecordConstraint
// | StringConstraint

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
): InvalidTypeError => ({
  type: 'invalidType',
  expected,
  actual,
  path: pathToString(path)
})

const emptyString = (path: string[]): EmptyStringError => ({
  type: 'emptyString',
  path: pathToString(path)
})

const mustBeGreater = (
  limit: number,
  actual: number,
  path: string[]
): MustBeGreaterThanError => ({
  type: 'mustBeGreater',
  limit,
  actual,
  path: pathToString(path)
})

const mustBeAtLeast = (
  limit: number,
  actual: number,
  path: string[]
): MustBeAtLeastError => ({
  type: 'mustBeAtLeast',
  limit,
  actual,
  path: pathToString(path)
})

const mustBeLesser = (
  limit: number,
  actual: number,
  path: string[]
): MustBeLesserThanError => ({
  type: 'mustBeLesser',
  limit,
  actual,
  path: pathToString(path)
})

const mustBeAtMost = (
  limit: number,
  actual: number,
  path: string[]
): MustBeAtMostError => ({
  type: 'mustBeAtMost',
  limit,
  actual,
  path: pathToString(path)
})

const noMatch = (
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

const noClassName = (data: any, path: string[]): NoClassNameError => ({
  type: 'noClassName',
  data,
  path: pathToString(path)
})
