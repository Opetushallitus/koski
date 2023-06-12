import * as $ from 'optics-ts'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { KoodiarvotOf } from './koodisto'

// Mikä tahansa luokka, joka on exportattu Scala-koodista
export type ObjWithClass = { $class: string }

// Palauttaa string-tyypin, joka on annetun Scala-luokan nimen literaali
export type ClassOf<T> = T extends ObjWithClass ? T['$class'] : never

// Sovittaa v2-tyypityksen vanhalle datamallille, josta puuttuu luokkien nimet
export type LegacyClass<T extends ObjWithClass> = Omit<T, '$class'>

export type CollectableOptic<S, A> =
  | $.Prism<S, any, A>
  | $.Traversal<S, any, A>
  | $.Fold<S, A>

export type ItemOf<S extends any[]> = S[0]

export type OpiskeluoikeudenTyyppiOf<T extends Opiskeluoikeus> = KoodiarvotOf<
  T['tyyppi']
>

export const schemaClassName = (fullClassName: string): string | null => {
  const match = fullClassName.match(/fi\.oph\.koski\.schema\.(.*)/)
  return match ? match[1] : null
}

export const shortClassName = (fullClassName: string): string => {
  const tokens = fullClassName.split('.')
  return tokens[tokens.length - 1]
}

export const isKoodistoOf =
  <T extends string>(koodistoUri: T) =>
  (koodiviite?: Koodistokoodiviite): koodiviite is Koodistokoodiviite<T> =>
    koodiviite?.koodistoUri === koodistoUri
