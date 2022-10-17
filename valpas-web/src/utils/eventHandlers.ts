import React from "react"

export const formSubmitHandler =
  (fn: (event: React.FormEvent<HTMLFormElement>) => void) =>
  (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    fn(event)
  }
