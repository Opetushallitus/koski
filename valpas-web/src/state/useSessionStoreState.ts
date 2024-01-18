import { Dispatch, SetStateAction, useMemo, useState } from "react"

export type StateStorage<S> = {
  get: () => S
  set: (value: S) => void
}

export const nonStoredState = <S>(initialState: S): StateStorage<S> => ({
  get() {
    return initialState
  },
  set(_) {},
})

export const sessionStateStorage = <S, T>(
  name: string,
  initialState: S,
  mapSave: (a: S) => T = (a: unknown) => a as T,
  mapRestore: (a: T) => S = (a: unknown) => a as S,
): StateStorage<S> => {
  return {
    get() {
      const storedState = sessionStorage.getItem(name)
      return storedState ? mapRestore(JSON.parse(storedState)) : initialState
    },
    set(value) {
      sessionStorage.setItem(name, JSON.stringify(mapSave(value)))
    },
  }
}

export const useStoredState = <S>(
  storage: StateStorage<S>,
): [S, Dispatch<SetStateAction<S>>] => {
  const initialState = useMemo(() => storage.get(), [storage])

  const [state, setState] = useState(initialState)

  const setAndStoreState = (value: SetStateAction<S>): void => {
    const newState: S = isFunction(value) ? value(state) : value
    setState(newState)
    storage.set(newState)
  }

  return [state, setAndStoreState]
}

const isFunction = <S>(s: SetStateAction<S>): s is (value: S) => S =>
  typeof s === "function"
