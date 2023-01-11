import React, { useContext, useMemo } from 'react'

export type DepthProvider = React.FC<React.PropsWithChildren>

const contexts: Record<string, React.Context<number>> = {}

export const useDepth = (key: string): [number, DepthProvider] => {
  if (!contexts[key]) {
    contexts[key] = React.createContext<number>(0)
  }
  const depth = useContext(contexts[key])

  const Provider = contexts[key].Provider
  const DepthProvider: DepthProvider = useMemo(
    () => (props) => {
      return <Provider value={depth + 1}>{props.children}</Provider>
    },
    [depth]
  )

  return [depth, DepthProvider]
}
