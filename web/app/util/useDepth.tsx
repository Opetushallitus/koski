import React, { useContext, useMemo } from 'react'

export type LayoutProvider = React.FC<
  React.PropsWithChildren<{
    indent?: number
    advanceRow?: number
  }>
>

export type LayoutPosition = {
  col: number
  row: number
}

const initialLayoutContext: LayoutPosition = {
  col: 0,
  row: 0
}

const contexts: Record<string, React.Context<LayoutPosition>> = {}

export const useLayout = (key: string): [LayoutPosition, LayoutProvider] => {
  if (!contexts[key]) {
    contexts[key] = React.createContext<LayoutPosition>(initialLayoutContext)
  }
  const layout = useContext(contexts[key])

  const Provider = contexts[key].Provider
  const LayoutProvider: LayoutProvider = useMemo(
    () => (props) => {
      return (
        <Provider
          value={{
            col: layout.col + (props.indent || 0),
            row: layout.row + (props.advanceRow || 0)
          }}
        >
          {props.children}
        </Provider>
      )
    },
    [layout]
  )

  return [layout, LayoutProvider]
}
