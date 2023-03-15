import React, { useEffect, useMemo, useState } from 'react'

export const useInterval = (fn: () => void, intervalMs: number) => {
  const [active, setActive] = useState(false)

  useEffect(() => {
    if (active) {
      const poller = setInterval(fn, intervalMs)
      return () => clearInterval(poller)
    }
  }, [active, fn, intervalMs])

  return useMemo(
    () => ({
      start: () => setActive(true),
      stop: () => setActive(false)
    }),
    []
  )
}
