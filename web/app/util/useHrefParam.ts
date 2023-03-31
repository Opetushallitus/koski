import { useMemo } from 'react'
import { parseQuery } from './url'

export const useHrefParam = (key: string): string | undefined => {
  const href = window.location.href
  return useMemo(() => parseQuery(href)[key], [href, key])
}
