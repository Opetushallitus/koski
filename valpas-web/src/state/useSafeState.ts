import {
  Dispatch,
  SetStateAction,
  useCallback,
  useEffect,
  useState,
} from "react"

export const useMounted = () => {
  const [mounted, setMounted] = useState(true)
  useEffect(() => () => setMounted(false), [])
  return mounted
}

export const useSafeStateWith = <S>(
  mounted: boolean,
  initialState: S | (() => S),
): [S, Dispatch<SetStateAction<S>>] => {
  const [state, setState] = useState(initialState)
  const safeSetState = useCallback(
    (newState: SetStateAction<S>) => {
      if (mounted) {
        setState(newState)
      }
    },
    [mounted],
  )

  return [state, safeSetState]
}

export const useSafeState = <S>(
  initialState: S | (() => S),
): [S, Dispatch<SetStateAction<S>>] => {
  const mounted = useMounted()
  return useSafeStateWith(mounted, initialState)
}
