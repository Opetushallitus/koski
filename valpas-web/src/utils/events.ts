import React from "react"

export const withoutDefaultAction = (f: () => void) => (
  e: React.MouseEvent<any, any> | React.KeyboardEvent
) => {
  e.preventDefault()
  e.stopPropagation()
  f()
}

export const stopPropagation = withoutDefaultAction(() => {})

export const onKbEscape = (f?: () => void) => (e: React.KeyboardEvent) => {
  if (e.key === "Escape") {
    f?.()
  }
}
