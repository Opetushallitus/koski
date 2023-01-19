import * as A from 'fp-ts/Array'
import { isArrayConstraint } from '../types/fi/oph/koski/typemodel/ArrayConstraint'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { isObjectConstraint } from '../types/fi/oph/koski/typemodel/ObjectConstraint'
import { isStringConstraint } from '../types/fi/oph/koski/typemodel/StringConstraint'

export const constraintHasProp = (
  constraint: Constraint | null,
  propKey: string
): boolean =>
  isObjectConstraint(constraint) && constraint.properties[propKey] !== undefined

// TODO: Heitä näissä kaikissa poikkeus, jos constraintin havaitaan olevan väärän tyyppinen constraint

export const constraintObjectProp =
  (...propNamePath: string[]) =>
  (constraint: Constraint | null): Constraint | null => {
    if (A.isEmpty(propNamePath)) {
      return constraint
    }
    const c =
      (isObjectConstraint(constraint) &&
        constraint.properties[propNamePath[0]]) ||
      null
    return c ? constraintObjectProp(...propNamePath.slice(1))(c) : null
  }

export const constraintObjectClass = (
  constraint: Constraint | null
): string | null => (isObjectConstraint(constraint) ? constraint.class : null)

export const constraintArrayItem = (
  constraint: Constraint | null
): Constraint | null =>
  isArrayConstraint(constraint) ? constraint.items : null

export const allowedStrings = (
  constraint: Constraint | null
): string[] | null =>
  (isStringConstraint(constraint) && constraint.enum) || null

export type KoodiviiteConstraint = {
  koodistoUri: string | null
  koodiarvot: string[] | null
}

export const koodiviiteConstraints = (
  constraint: Constraint | null
): KoodiviiteConstraint | null =>
  isObjectConstraint(constraint) &&
  constraint.class === 'fi.oph.koski.schema.Koodistokoodiviite'
    ? {
        koodistoUri:
          allowedStrings(
            constraintObjectProp('koodistoUri')(constraint)
          )?.[0] || null,
        koodiarvot:
          allowedStrings(constraintObjectProp('koodiarvo')(constraint)) || null
      }
    : null
