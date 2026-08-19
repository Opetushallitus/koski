import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import * as O from 'fp-ts/Option'
import { ISO2FinnishDate } from '../../date/date'
import {
  isLocalizedString,
  LocalizedString
} from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  ArrayConstraint,
  isArrayConstraint
} from '../../types/fi/oph/koski/typemodel/ArrayConstraint'
import {
  BooleanConstraint,
  isBooleanConstraint
} from '../../types/fi/oph/koski/typemodel/BooleanConstraint'
import { Constraint } from '../../types/fi/oph/koski/typemodel/Constraint'
import {
  DateConstraint,
  isDateConstraint
} from '../../types/fi/oph/koski/typemodel/DateConstraint'
import {
  isLiteralConstraint,
  LiteralConstraint
} from '../../types/fi/oph/koski/typemodel/LiteralConstraint'
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
  isRecordConstraint,
  RecordConstraint
} from '../../types/fi/oph/koski/typemodel/RecordConstraint'
import {
  isStringConstraint,
  StringConstraint
} from '../../types/fi/oph/koski/typemodel/StringConstraint'
import {
  isUnionConstraint,
  UnionConstraint
} from '../../types/fi/oph/koski/typemodel/UnionConstraint'
import { nonFalsy } from '../../util/fp/arrays'
import { ValidationRule } from './ValidationRule'

export type ValidationError =
  | InvalidTypeError
  | EmptyStringError
  | EmptyValueError
  | NoMatchingValueError<any>
  | InvalidDateError
  | MustBeGreaterThanError
  | MustBeAtLeastError
  | MustBeLesserThanError
  | MustBeAtMostError
  | NoMatchingClassError
  | NoClassNameError
  | ArviointiPuuttuuValmiiltaSuoritukseltaError

export const isValidationError = (a: any): a is ValidationError =>
  typeof a === 'object' && typeof a.type === 'string'

/**
 * Kohdistuuko jokin annetuista virheistä nimettyyn alikenttään?
 *
 * Kenttäeditori saa virheet koko olionsa alta, koska useFormErrors kerää
 * kaikki polun alkuiset virheet. Esimerkiksi aikajakson editori näkee sekä
 * alku- että loppupäivän virheet, joten oikean syötekentän värjäämiseksi ne on
 * eroteltava polun viimeisen osan perusteella.
 */
export const hasErrorInField = (
  errors: ValidationError[] | undefined,
  field: string
): boolean => (errors || []).some((e) => e.path.split('.').pop() === field)

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

export type EmptyValueError = {
  type: 'emptyValue'
  path: string
}

/**
 * Osasuoritukselta puuttuu arviointi, vaikka päätason suoritus on vahvistettu.
 * Vastaa palvelimen validaatiota "Valmiiksi merkityllä suorituksella on
 * keskeneräinen osasuoritus", mutta kohdistuu suoraan puuttuvaan kenttään.
 */
export type ArviointiPuuttuuValmiiltaSuoritukseltaError = {
  type: 'arviointiPuuttuuValmiiltaSuoritukselta'
  path: string
}

export type NoMatchingValueError<T> = {
  type: 'noMatchingValue'
  path: string
  expected: T[]
  actual: T
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

export type NoMatchingClassError = {
  type: 'noMatchingClass'
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

/**
 * Validoi datan skeemaa (constraint) vasten ja ajaa lisäksi annetut säännöt.
 *
 * Säännöt ajetaan *jokaisessa solmussa*, jonka läpi skeemavalidointi kulkee, eli sääntö saa
 * isMatch- ja validate-funktioihinsa kyseisen kohdan datan ja polun. Näin sääntö voi kohdistaa
 * virheensä suoraan oikeaan kenttään ilman että sen tarvitsee kävellä koko datarakennetta itse.
 *
 * @param data Validoitava data
 * @param constraint Skeema, tai null jos validoidaan pelkillä säännöillä
 * @param rules Skeeman ulkopuoliset lisävalidoinnit
 */
export const validateData = (
  data: unknown,
  constraint?: Constraint | null,
  rules: ValidationRule<any>[] = []
): ValidationError[] => validate(data, constraint, [], rules)

const validate = (
  data: unknown,
  constraint: Constraint | null | undefined,
  path: string[],
  rules: ValidationRule<any>[]
): ValidationError[] => {
  // Ei sääntöjä: vältetään turha allokointi jokaisessa solmussa.
  if (A.isEmpty(rules)) {
    return validateConstraint(data, constraint, path, rules)
  }
  const ruleErrors = rules.flatMap((rule) =>
    rule.isMatch(data, path) ? rule.validate(data, path) : []
  )
  const constraintErrors = validateConstraint(data, constraint, path, rules)
  return A.isEmpty(ruleErrors)
    ? constraintErrors
    : [...ruleErrors, ...constraintErrors]
}

const validateConstraint = (
  data: unknown,
  constraint: Constraint | null | undefined,
  path: string[],
  rules: ValidationRule<any>[]
): ValidationError[] => {
  if (isLocalizedString(data)) {
    return validateLocalizationString(data, constraint, path)
  } else if (isOptionalConstraint(constraint)) {
    return validateOptional(data, constraint, path, rules)
  } else if (!constraint) {
    return []
  } else if (data === null || data === undefined) {
    // Pakollinen arvo puuttuu. Tämä on tyypillisin syöttökentän virhe, joten sitä ei raportoida
    // tyyppivirheenä vaan tyhjänä arvona.
    return [emptyValue(path)]
  } else if (isObjectConstraint(constraint)) {
    return validateObject(data, constraint, path, rules)
  } else if (isArrayConstraint(constraint)) {
    return validateArray(data, constraint, path, rules)
  } else if (isUnionConstraint(constraint)) {
    return validateUnion(data, constraint, path, rules)
  } else if (isNumberConstraint(constraint)) {
    return validateNumber(data, constraint, path)
  } else if (isStringConstraint(constraint)) {
    return validateString(data, constraint, path)
  } else if (isBooleanConstraint(constraint)) {
    return validateBoolean(data, constraint, path)
  } else if (isDateConstraint(constraint)) {
    return validateDate(data, constraint, path)
  } else if (isLiteralConstraint(constraint)) {
    return validateLiteral(data, constraint, path)
  } else if (isRecordConstraint(constraint)) {
    return validateRecord(data, constraint, path, rules)
  }
  return []
}

// ObjectConstraint
const validateObject = (
  data: unknown,
  constraint: ObjectConstraint,
  path: string[],
  rules: ValidationRule<any>[]
): ValidationError[] => {
  if (typeof data !== 'object') {
    return [invalidType('object', data, path)]
  } else {
    return Object.entries(constraint.properties).flatMap(([key, child]) =>
      validate((data as any)[key], child, [...path, key], rules)
    )
  }
}

// ArrayConstraint
const validateArray = (
  data: unknown,
  constraint: ArrayConstraint,
  path: string[],
  rules: ValidationRule<any>[]
): ValidationError[] => {
  if (!Array.isArray(data)) {
    return [invalidType('array', data, path)]
  } else {
    return data.flatMap((e, i) =>
      validate(e, constraint.items, [...path, i.toString()], rules)
    )
  }
}

// UnionConstraint
const validateUnion = (
  data: unknown,
  constraint: UnionConstraint,
  path: string[],
  rules: ValidationRule<any>[]
): ValidationError[] => {
  const className = (data as any)?.$class as string
  if (!className) {
    return [
      noMatchingClass(Object.keys(constraint.anyOf), className, data, path)
    ]
  }
  const childC = constraint.anyOf[className]
  if (!childC) {
    return [
      noMatchingClass(Object.keys(constraint.anyOf), className, data, path)
    ]
  }
  return validateConstraint(data, childC, path, rules)
}

// OptionalConstraint
const validateOptional = (
  data: unknown,
  constraint: OptionalConstraint,
  path: string[],
  rules: ValidationRule<any>[]
): ValidationError[] => {
  if (data === null || data === undefined) {
    return []
  }
  return validateConstraint(data, constraint.optional, path, rules)
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

// StringConstraint
const validateString = (
  data: unknown,
  constraint: StringConstraint,
  path: string[]
): ValidationError[] => {
  if (typeof data === 'string') {
    return [
      data.length === 0 && emptyString(path),
      constraint.enum &&
        A.isNonEmpty(constraint.enum) &&
        !constraint.enum.includes(data) &&
        noMatchingValue(constraint.enum, data, path)
    ].filter(nonFalsy)
  }
  return [invalidType('string', data, path)]
}

// BooleanConstraint

const validateBoolean = (
  data: unknown,
  constraint: BooleanConstraint,
  path: string[]
): ValidationError[] => {
  if (typeof data === 'boolean') {
    return [
      constraint.enum &&
        A.isNonEmpty(constraint.enum) &&
        constraint.enum.includes(data) &&
        noMatchingValue(constraint.enum, data, path)
    ].filter(nonFalsy)
  }
  return [invalidType('boolean', data, path)]
}

// DateConstraint

const validateDate = (
  data: unknown,
  _constraint: DateConstraint,
  path: string[]
): ValidationError[] => {
  if (typeof data === 'string') {
    // Tyhjä kenttä ei ole virheellinen päivämäärä vaan täyttämätön. Ero näkyy
    // käyttäjälle: uusi jakso luodaan tyhjillä päivämäärillä (ks. uusiJakso.ts),
    // jolloin "Virheellinen päivämäärä" syyttäisi kirjoitusvirheestä, jota ei
    // ole tehty.
    if (data === '') {
      return [emptyString(path)]
    }
    return [ISO2FinnishDate(data) ? null : invalidDate(data, path)].filter(
      nonFalsy
    )
  }
  return [invalidType('ISO 8601 date string', data, path)]
}

// LiteralConstraint

const validateLiteral = (
  data: unknown,
  constraint: LiteralConstraint,
  path: string[]
): ValidationError[] => {
  if (typeof data === 'string') {
    return [
      data === constraint.constant
        ? null
        : noMatchingValue([constraint.constant], data, path)
    ].filter(nonFalsy)
  }
  return [invalidType('string', data, path)]
}

// RecordConstraint
const validateRecord = (
  data: unknown,
  constraint: RecordConstraint,
  path: string[],
  rules: ValidationRule<any>[]
): ValidationError[] => {
  if (typeof data !== 'object') {
    return [invalidType('object', data, path)]
  } else {
    return Object.entries(data as object).flatMap(([key, value]) =>
      validate(value, constraint.items, [...path, key], rules)
    )
  }
}

// LocalizationString
const validateLocalizationString = (
  str: LocalizedString,
  constraint: any,
  path: string[]
): ValidationError[] => {
  if ((str as any).fi || (str as any).sv || str.en) {
    return []
  } else {
    return isOptionalConstraint(constraint) ? [] : [emptyString(path)]
  }
}

// Error builders

/**
 * Virhepolun esitysmuoto. ValidationRule-toteutusten kannattaa muodostaa virhepolkunsa tällä
 * saamastaan polusta, jotta useFormErrors löytää virheen oikealle kentälle.
 */
export const pathToString = (path: string[]): string => path.join('.')

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

export const emptyValue = (path: string[]): EmptyValueError => ({
  type: 'emptyValue',
  path: pathToString(path)
})

export const noMatchingValue = <T>(
  expected: T[],
  actual: T,
  path: string[]
): NoMatchingValueError<T> => ({
  type: 'noMatchingValue',
  path: pathToString(path),
  expected,
  actual
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

export const noMatchingClass = (
  expected: string[],
  actual: any,
  data: any,
  path: string[]
): NoMatchingClassError => ({
  type: 'noMatchingClass',
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
