import React, { useCallback, useContext } from 'react'

export type LayoutProvider = React.FC<
  React.PropsWithChildren<{
    indent?: number
  }>
>

export type LayoutPosition = number

const initialLayoutContext: LayoutPosition = 0

const contexts: Record<string, React.Context<LayoutPosition>> = {}

export const useLayout = (key: string): [LayoutPosition, LayoutProvider] => {
  if (!contexts[key]) {
    contexts[key] = React.createContext<LayoutPosition>(initialLayoutContext)
  }
  const indentation = useContext(contexts[key])

  const Provider = contexts[key].Provider
  const LayoutProvider: LayoutProvider = useCallback(
    (props) => (
      <Provider value={indentation + (props.indent || 0)}>
        {props.children}
      </Provider>
    ),
    [indentation, Provider]
  )

  return [indentation, LayoutProvider]
}
