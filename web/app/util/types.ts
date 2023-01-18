import * as $ from 'optics-ts'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'

export type ObjWithClass = { $class: string }

export type ClassOf<T extends ObjWithClass> = T['$class']

export type KoodistoUriOf<T extends Koodistokoodiviite> = T['koodistoUri']

export type KoodiarvotOf<T extends Koodistokoodiviite> = T['koodiarvo']

export const schemaClassName = (fullClassName: string): string | null => {
  const match = fullClassName.match(/fi\.oph\.koski\.schema\.(.*)/)
  return match ? match[1] : null
}

export type CollectableOptic<S, A> =
  | $.Prism<S, any, A>
  | $.Traversal<S, any, A>
  | $.Fold<S, A>

export const isKoodistoOf =
  <T extends string>(koodistoUri: T) =>
  (koodiviite?: Koodistokoodiviite): koodiviite is Koodistokoodiviite<T> =>
    koodiviite?.koodistoUri === koodistoUri
