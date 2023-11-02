import * as A from 'fp-ts/lib/Array'
import * as string from 'fp-ts/lib/string'
import { useEffect, useState } from 'react'

const strDifference = A.difference(string.Eq)

export const useNewItems = <T>(getId: (t: T) => string, items?: T[]) => {
  const [initialIds, setInitialIds] = useState<string[]>()

  useEffect(() => {
    if (items && (!initialIds || items.length < initialIds.length)) {
      setInitialIds(items.map(getId))
    }
  }, [getId, initialIds, items])

  return initialIds && items ? strDifference(initialIds)(items.map(getId)) : []
}
