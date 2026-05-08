import * as A from 'fp-ts/Array'
import * as Eq from 'fp-ts/Eq'
import * as string from 'fp-ts/string'
import { nonNull } from '../util/fp/arrays'

const distinctKoodistoUri = A.uniq(string.Eq)

export const uniqueKoodistoUris = (
  koodistoUris: Array<string | null | undefined>
): string[] => distinctKoodistoUri(koodistoUris.filter(nonNull))

export const uniqueKoodistot = <T extends { id: string }>(
  koodistot: T[]
): T[] => A.uniq(Eq.fromEquals<T>((x, y) => x.id === y.id))(koodistot)
