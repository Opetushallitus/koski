import { useMemo } from 'react'

export const useSearchParam = (key: string): string | null =>
  useMemo(
    () => new URLSearchParams(window.location.search).get(key),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [key, window.location.search]
  )

export const useVersionumero = () => useSearchParam('versionumero')
