import bem from "bem-ts"
import React, { FormEvent } from "react"
import { joinClassNames } from "../../utils/classnames"
import "./forms.less"

const b = bem("form")

export type FormProps = React.HTMLAttributes<HTMLFormElement>

export const Form = ({ className, onSubmit, ...props }: FormProps) => {
  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onSubmit?.(event)
  }

  return (
    <form
      className={joinClassNames(b(), className)}
      onSubmit={submit}
      {...props}
    />
  )
}
