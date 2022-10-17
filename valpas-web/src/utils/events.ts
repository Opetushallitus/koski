import React from "react"

export const withoutDefaultAction =
  (f: () => void) => (e: React.MouseEvent<any, any> | React.KeyboardEvent) => {
    e.preventDefault()
    e.stopPropagation()
    f()
  }

export const stopPropagation = withoutDefaultAction(() => {})

export const kbSwitch =
  (...handlers: Array<(e: React.KeyboardEvent) => boolean>) =>
  (e: React.KeyboardEvent): boolean => {
    for (let handler of handlers) {
      if (handler(e)) {
        return true
      }
    }
    return false
  }

const onKeys =
  (keys: string[]) =>
  (f?: () => void) =>
  (e: React.KeyboardEvent): boolean => {
    if (keys.includes(e.key)) {
      f?.()
      return true
    }
    return false
  }

export const onKbSelect = onKeys(["Enter", " "])
export const onKbEscape = onKeys(["Escape"])
