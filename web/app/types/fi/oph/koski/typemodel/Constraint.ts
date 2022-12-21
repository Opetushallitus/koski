import { AnyConstraint, isAnyConstraint } from './AnyConstraint'
import { ArrayConstraint, isArrayConstraint } from './ArrayConstraint'
import { BooleanConstraint, isBooleanConstraint } from './BooleanConstraint'
import { DateConstraint, isDateConstraint } from './DateConstraint'
import { LiteralConstraint, isLiteralConstraint } from './LiteralConstraint'
import { NumberConstraint, isNumberConstraint } from './NumberConstraint'
import { ObjectConstraint, isObjectConstraint } from './ObjectConstraint'
import { OptionalConstraint, isOptionalConstraint } from './OptionalConstraint'
import { RecordConstraint, isRecordConstraint } from './RecordConstraint'
import { StringConstraint, isStringConstraint } from './StringConstraint'
import { UnionConstraint, isUnionConstraint } from './UnionConstraint'

/**
 * Constraint
 *
 * @see `fi.oph.koski.typemodel.Constraint`
 */
export type Constraint =
  | AnyConstraint
  | ArrayConstraint
  | BooleanConstraint
  | DateConstraint
  | LiteralConstraint
  | NumberConstraint
  | ObjectConstraint
  | OptionalConstraint
  | RecordConstraint
  | StringConstraint
  | UnionConstraint

export const isConstraint = (a: any): a is Constraint =>
  isAnyConstraint(a) ||
  isArrayConstraint(a) ||
  isBooleanConstraint(a) ||
  isDateConstraint(a) ||
  isLiteralConstraint(a) ||
  isNumberConstraint(a) ||
  isObjectConstraint(a) ||
  isOptionalConstraint(a) ||
  isRecordConstraint(a) ||
  isStringConstraint(a) ||
  isUnionConstraint(a)
