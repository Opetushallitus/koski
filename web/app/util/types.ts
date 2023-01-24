import * as $ from 'optics-ts'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'

// Mikä tahansa luokka, joka on exportattu Scala-koodista
export type ObjWithClass = { $class: string }

// Palauttaa string-tyypin, joka on annetun Scala-luokan nimen literaali
export type ClassOf<T extends ObjWithClass> = T['$class']

// Sovittaa v2-tyypityksen vanhalle datamallille, josta puuttuu luokkien nimet
export type LegacyClass<T extends ObjWithClass> = Omit<T, '$class'>

export type KoodistoUriOf<T extends Koodistokoodiviite> = T['koodistoUri']

export type KoodiarvotOf<T extends Koodistokoodiviite> = T['koodiarvo']

export type CollectableOptic<S, A> =
  | $.Prism<S, any, A>
  | $.Traversal<S, any, A>
  | $.Fold<S, A>

export const schemaClassName = (fullClassName: string): string | null => {
  const match = fullClassName.match(/fi\.oph\.koski\.schema\.(.*)/)
  return match ? match[1] : null
}

export const isKoodistoOf =
  <T extends string>(koodistoUri: T) =>
  (koodiviite?: Koodistokoodiviite): koodiviite is Koodistokoodiviite<T> =>
    koodiviite?.koodistoUri === koodistoUri
