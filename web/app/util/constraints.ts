import * as A from 'fp-ts/Array'
import * as Eq from 'fp-ts/Eq'
import { pipe } from 'fp-ts/lib/function'
import { isArrayConstraint } from '../types/fi/oph/koski/typemodel/ArrayConstraint'
import {
  Constraint,
  isConstraint
} from '../types/fi/oph/koski/typemodel/Constraint'
import { isObjectConstraint } from '../types/fi/oph/koski/typemodel/ObjectConstraint'
import { isOptionalConstraint } from '../types/fi/oph/koski/typemodel/OptionalConstraint'
import {
  isStringConstraint,
  StringConstraint
} from '../types/fi/oph/koski/typemodel/StringConstraint'
import {
  isUnionConstraint,
  UnionConstraint
} from '../types/fi/oph/koski/typemodel/UnionConstraint'
import { nonNull } from './fp/arrays'
import { ClassOf, ObjWithClass, schemaClassName, shortClassName } from './types'
import { isAnyConstraint } from '../types/fi/oph/koski/typemodel/AnyConstraint'
import { intersects } from './array'
import { isRefConstraint } from '../types/fi/oph/koski/typemodel/RefConstraint'
import { deepEqual } from './fp/objects'

/**
 * Muuta yksittäinen constraint listaksi. Palauttaa null, jos annettu arvo on null.
 *
 * Useimmat constrainteihin liittyvät funktiot käsittelevä† listoja, jotta niiden ketjutus olisi helpompaa.
 */
export const asList = (c: Constraint | null): Constraint[] | null =>
  c ? (isUnionConstraint(c) ? Object.values(c.anyOf) : [c]) : null

/**
 * Ottaa listasta sen ainoan arvon. Heittää poikkeuksen, jos taulukon koko muuta kuin 1.
 */
export const singular = <T>(c: T[] | null): T | null => {
  if (!c) {
    return null
  }
  if (c.length === 0) {
    throw new Error('Tried to take an item from empty set')
  }
  if (c.length > 1) {
    throw new Error(
      `Tried to take a single item from a set of ${c.length} items: [${c
        .map((a) => (isConstraint(a) ? toString(a) : `${a}`))
        .join(', ')}]`
    )
  }
  return c[0]
}

/**
 * Flatmap-toteutus constraint-listalle. Palauttaa null, jos yksikin listan alkioista on null.
 */
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

/**
 * Hakee constraintin lapsen merkkijonolla ilmaistavan polun perusteella.
 * Esimerkki polusta: `"lapset.[].nimi"`, jossa *lapset* ja *nimi* ovat propertyn nimiä ja *[]* ilmaisee taulukon kaikkia jäseniä.
 *
 * Heittää poikkeuksen, jos annettua polkua ei pysty seuraamaan.
 *
 * @see prop
 * @see elems
 *
 * @params pathStr Polku merkkijonomuodossa.
 * @returns Lapsi-constraint. Null, jos annettu argumentti oli null.
 */
export const path =
  (pathStr: string) =>
  (constraints: Constraint[] | null): Constraint[] | null => {
    if (constraints) {
      const fns: Array<
        [string, (cs: Constraint[] | null) => Constraint[] | null]
      > = pathStr
        .split('.')
        .map((key) => (key === '[]' ? [key, elems] : [key, props(key)]))
      return flatMap((constraint: Constraint | null): Constraint[] | null =>
        fns.reduce((acc, [key, fn]) => {
          try {
            return fn(acc)
          } catch (err) {
            throw new Error(
              `Invalid ${toString(constraint)} path '${pathStr}' at ${key}: ${err}`
            )
          }
        }, asList(constraint))
      )(constraints)
    }
    return null
  }

/**
 * Palauttaa true, jos annettu *constraint* viittaa objektiin, jolla on property *propKey*.
 * @param constraint
 * @param propKey
 * @returns boolean
 */
export const hasProp = (
  constraint: Constraint | null,
  propKey: string
): boolean =>
  isObjectConstraint(constraint) && constraint.properties[propKey] !== undefined

/**
 * Palauttaa funktion, joka palauttaa annetun constraintin lapsipropertyn.
 *
 * Heittää poikkeuksen, jos propertya ei ole.
 *
 * @see props
 *
 * @param propNamePath Propertyn nimi (voi antaa myös alapropertyjen nimiä lisäargumentteina)
 * @returns Lapsi-constraint. Null, jos annettu argumentti oli null.
 */
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
    if (isAnyConstraint(constraint)) {
      return []
    }
    throw new Error(`${toString(constraint)} cannot have any properties`)
  }

/**
 * Palauttaa funktion, joka palauttaa annetun constraint-listan propertynimen mukaiset lapsi-constraintit.
 *
 * @see prop
 */
export const props = (...propNamePath: string[]) =>
  flatMap(prop(...propNamePath))

/**
 * Palauttaa constraintin määrittelemän luokan nimen (objektille, eli constraint viittaa yksittäiseen luokkaan)
 * tai monta nimeä (unionille, eli constraint viittaa traitiin).
 *
 * Heittää poikkeuksen, jos constraint osoittaa tietotyyppiin, jolla ei ole nimeä.
 */
export const className =
  <T extends ObjWithClass>() =>
  (constraint: Constraint | null): ClassOf<T>[] | null => {
    if (!constraint) {
      return null
    }
    if (isObjectConstraint(constraint)) {
      // TODO: Käy tyypitys läpi
      // @ts-expect-error
      return [constraint.class]
    }
    if (isOptionalConstraint(constraint)) {
      return className<T>()(constraint.optional)
    }
    if (isUnionConstraint(constraint)) {
      // TODO: Käy tyypitys läpi
      // @ts-expect-error
      return Object.keys(constraint.anyOf)
    }
    throw new Error(`${toString(constraint)} does not have a class name`)
  }

/**
 * Palauttaa constraintien määrittelemät luokan nimet.
 *
 * Heittää poikkeuksen, jos constraint osoittaa tietotyyppiin, jolla ei ole nimeä.
 *
 * @see className
 */
export const classNames = <T extends ObjWithClass>() => flatMap(className<T>())

/**
 * Palauttaa listaan osoittavan constraintin lapsiconstraintin.
 *
 * Heittää poikkeuksen, jos constraint ei osoita listaan.
 */
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
  throw new Error(`${toString(constraint)} is not an array`)
}

/**
 * Palauttaa listaan osoittavien constraintien lapsiconstraintit.
 *
 * Heittää poikkeuksen, jos yksikin constraint ei osoita listaan.
 *
 * @see elem
 */
export const elems = flatMap(elem)

/**
 * Palauttaa merkkijonoon osoittavan constraintin sallitut merkkijonot.
 * Palauttaa tyhjän listan, jos rajoituksia ei ole määritelty.
 *
 * Heittää poikkeuksen, jos constraint ei osoita merkkijonoon.
 */
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
  throw new Error(`${toString(constraint)} is not a string`)
}

/**
 * Palauttaa merkkijonoon osoittavien constraintien sallitut merkkijonot.
 * Palauttaa tyhjän listan, jos rajoituksia ei ole määritelty.
 *
 * Heittää poikkeuksen, jos constraint ei osoita merkkijonoon.
 *
 * @see allowedStrings
 */
export const allAllowedStrings = flatMap(allowedStrings)

export type KoodiviiteConstraint<T extends string> = {
  koodistoUri: T | null
  koodiarvot: string[] | null
}

/**
 * Palauttaa koodistokoodiviitteeseen viittavan constraintin sallitut koodiarvot ja urit.
 *
 * Heittää poikkeuksen, jos constraint ei osoita koodistokoodiviitteeseen.
 */
export const koodiviite = <T extends string>(
  constraint: Constraint | null
): Array<KoodiviiteConstraint<T>> | null => {
  if (!constraint) {
    return null
  }
  if (isUnionConstraint(constraint)) {
    return Object.values(constraint.anyOf)
      .flatMap(koodiviite)
      .filter(nonNull) as Array<KoodiviiteConstraint<T>>
  }
  if (
    isObjectConstraint(constraint) &&
    constraint.class === 'fi.oph.koski.schema.Koodistokoodiviite'
  ) {
    const koodistoUris = allowedStrings(
      singular(prop('koodistoUri')(constraint))
    )
    const koodiarvot =
      allowedStrings(singular(prop('koodiarvo')(constraint))) || null

    return (koodistoUris?.map((koodistoUri) => ({
      koodistoUri,
      koodiarvot
    })) || null) as Array<KoodiviiteConstraint<T>> | null
  }
  throw new Error(`${toString(constraint)} is not Object<Koodistokoodiviite>`)
}

/**
 * Poistaa constraintin ympäriltä käärivät constraintit (esim. taulukko tai optional).
 * Jos sen ympärillä ei ole käärettä, palautetaan annettu malli sellaisenaan.
 */
export const flatten = (constraint: Constraint | null): Constraint | null => {
  if (isArrayConstraint(constraint)) {
    return flatten(constraint.items)
  }
  if (isOptionalConstraint(constraint)) {
    return flatten(constraint.optional)
  }
  if (isUnionConstraint(constraint)) {
    const anyOf = Object.values(constraint.anyOf)
    if (anyOf.length === 1) {
      return anyOf[0]
    }
  }
  return constraint
}

/**
 * Poistaa constraintin ympäriltä kaikki käärivät constraintit (esim. taulukko tai optional).
 * Jos sen ympärillä ei ole käärettä, palautetaan annettu malli sellaisenaan.
 */
export const deepFlatten = (
  constraint: Constraint | null
): Constraint | null => {
  const flat = flatten(constraint)
  return flat === constraint ? flat : deepFlatten(flat)
}

export const filter =
  (cond: (c: Constraint) => boolean) =>
  (constraints: Constraint[] | null): Constraint[] | null =>
    constraints && constraints.flatMap((c) => asList(c)!.filter(cond))

export const satisfiesEnum =
  (...values: string[]) =>
  (constraint: Constraint): constraint is StringConstraint =>
    isStringConstraint(constraint) &&
    (constraint.enum === undefined ||
      values.length === 0 ||
      intersects(constraint.enum, values))

export const filterKoodistokoodiviite =
  (uri: string, koodiarvot?: string[]) =>
  (koodistot: Constraint[] | null): Constraint[] | null =>
    koodistot &&
    filter((kc) => {
      if (isObjectConstraint(kc)) {
        const uriMatch = prop('koodistoUri')(kc)?.find(satisfiesEnum(uri))
        const koodiarvoMatch = prop('koodiarvo')(kc)?.find(
          satisfiesEnum(...(koodiarvot || []))
        )
        return !!uriMatch && !!koodiarvoMatch
      }
      return false
    })(koodistot)

export const join = (constraints: Constraint[] | null) =>
  constraints
    ? constraints.length === 1
      ? constraints[0]
      : UnionConstraint({
          anyOf: Object.fromEntries(
            distinct(constraints).map((c, i) => [i.toString(), c])
          )
        })
    : null

export const isNonEmpty = (constraints: Constraint[] | null): boolean =>
  !!constraints && constraints.length > 0

export const distinct = (cs: Constraint[]): Constraint[] =>
  A.uniq(ConstraintEq)(cs)

export const ConstraintEq = Eq.fromEquals<Constraint>(deepEqual)

/**
 * Tekee constraintista ihmisystävällisen merkkijonon. Käytetään lähinnä virheilmoituksiin kehittäjille.
 */
export const toString = (constraint: Constraint | null): string => {
  if (!constraint) {
    return 'null'
  }
  if (isObjectConstraint(constraint)) {
    return `Object<${shortClassName(constraint.class)}>`
  }
  if (isArrayConstraint(constraint)) {
    return `Array<${toString(constraint.items)}>`
  }
  if (isOptionalConstraint(constraint)) {
    return `Optional<${toString(constraint.optional)}>`
  }
  if (isRefConstraint(constraint)) {
    return `ref to Object<${shortClassName(constraint.className)}>`
  }
  if (isUnionConstraint(constraint)) {
    return Object.values(constraint.anyOf).map(toString).join(' | ')
  }
  return shortClassName(constraint.$class)
}
