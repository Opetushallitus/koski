import React from "react"

export const withoutDefaultAction = (f: () => void) => (
  e: React.MouseEvent<any, any>
) => {
  e.preventDefault()
  e.stopPropagation()
  f()
}
