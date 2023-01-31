import { useEffect } from 'react'

export const useDebounce = <A extends any[]>(
  milliseconds: number,
  fn: (...args: A) => void,
  deps: A
) => {
  useEffect(() => {
    const timeout = setTimeout(() => fn(...deps), milliseconds)
    return () => clearTimeout(timeout)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)
}
