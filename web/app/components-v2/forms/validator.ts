import { number } from 'prop-types'
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
}

export type EmptyStringError = {
  type: 'emptyString'
}

export type MustBeGreaterThanError = {
  type: 'mustBeGreater'
  limit: number
  actual: number
}

export type MustBeAtLeastError = {
  type: 'mustBeAtLeast'
  limit: number
  actual: number
}

export type MustBeLesserThanError = {
  type: 'mustBeLesser'
  limit: number
  actual: number
}

export type MustBeAtMostError = {
  type: 'mustBeAtMost'
  limit: number
  actual: number
}

export type NoMatchError = {
  type: 'noMatch'
  expected: string[]
  actual: any
  data: any
}

export type NoClassNameError = {
  type: 'noClassName'
  data: any
}

export const validateData = (
  data: unknown,
  constraint: Constraint
): ValidationError[] => {
  if (isLocalizedString(data)) {
    return validateLocalizationString(data)
  } else if (isObjectConstraint(constraint)) {
    return validateObject(data, constraint)
  } else if (isArrayConstraint(constraint)) {
    return validateArray(data, constraint)
  } else if (isUnionConstraint(constraint)) {
    return validateUnion(data, constraint)
  } else if (isOptionalConstraint(constraint)) {
    return validateOptional(data, constraint)
  } else if (isNumberConstraint(constraint)) {
    return validateNumber(data, constraint)
  }
  return []
}

// ObjectConstraint
const validateObject = (
  data: unknown,
  constraint: ObjectConstraint
): ValidationError[] => {
  if (typeof data !== 'object') {
    return [invalidType('object', data)]
  } else if (data === null || data === undefined) {
    return [invalidType('object', data)]
  } else if ((data as any).$class === undefined) {
    return [noClassName(data)]
  } else {
    return Object.entries(constraint.properties)
      .map(([key, child]) => validateData((data as any)[key], child))
      .flat()
  }
}

// ArrayConstraint
const validateArray = (
  data: unknown,
  constraint: ArrayConstraint
): ValidationError[] => {
  if (!Array.isArray(data)) {
    return [invalidType('array', data)]
  } else {
    return data.flatMap((e) => validateData(e, constraint.items))
  }
}

// UnionConstraint
const validateUnion = (
  data: unknown,
  constraint: UnionConstraint
): ValidationError[] => {
  const className = (data as any)?.$class as string
  if (!className) {
    return [noMatch(Object.keys(constraint.anyOf), className, data)]
  }
  const childC = constraint.anyOf[className]
  if (!childC) {
    return [noMatch(Object.keys(constraint.anyOf), className, data)]
  }
  return validateData(data, childC)
}

// OptionalConstraint
const validateOptional = (
  data: unknown,
  constraint: OptionalConstraint
): ValidationError[] => {
  if (data === null || data === undefined) {
    return []
  }
  return validateData(data, constraint.optional)
}

// NumberConstraint
const validateNumber = (
  data: unknown,
  constraint: NumberConstraint
): ValidationError[] => {
  if (typeof data === 'number') {
    return [
      constraint.min &&
        constraint.min.inclusive &&
        data < constraint.min.n &&
        mustBeAtLeast(constraint.min.n, data),
      constraint.min &&
        !constraint.min.inclusive &&
        data <= constraint.min.n &&
        mustBeGreater(constraint.min.n, data),
      constraint.max &&
        constraint.max.inclusive &&
        data > constraint.max.n &&
        mustBeAtMost(constraint.max.n, data),
      constraint.max &&
        !constraint.max.inclusive &&
        data >= constraint.max.n &&
        mustBeLesser(constraint.max.n, data)
    ].filter(nonFalsy)
  }
  return [invalidType('number', data)]
}

// TODO: Implement rest of the constraints
// | AnyConstraint
// | BooleanConstraint
// | DateConstraint
// | LiteralConstraint
// | RecordConstraint
// | StringConstraint

// LocalizationString
const validateLocalizationString = (str: LocalizedString): ValidationError[] =>
  (str as any).fi || (str as any).sv || str.en ? [] : [emptyString]

// Error builders

const invalidType = (expected: string, actual: any): InvalidTypeError => ({
  type: 'invalidType',
  expected,
  actual
})

const emptyString: EmptyStringError = {
  type: 'emptyString'
}

const mustBeGreater = (
  limit: number,
  actual: number
): MustBeGreaterThanError => ({
  type: 'mustBeGreater',
  limit,
  actual
})

const mustBeAtLeast = (limit: number, actual: number): MustBeAtLeastError => ({
  type: 'mustBeAtLeast',
  limit,
  actual
})

const mustBeLesser = (
  limit: number,
  actual: number
): MustBeLesserThanError => ({
  type: 'mustBeLesser',
  limit,
  actual
})

const mustBeAtMost = (limit: number, actual: number): MustBeAtMostError => ({
  type: 'mustBeAtMost',
  limit,
  actual
})

const noMatch = (expected: string[], actual: any, data: any): NoMatchError => ({
  type: 'noMatch',
  expected,
  actual,
  data
})

const noClassName = (data: any): NoClassNameError => ({
  type: 'noClassName',
  data
})
