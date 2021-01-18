import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./forms.less"

const b = bem("form")

export type FormProps = React.HTMLAttributes<HTMLFormElement>

export const Form = ({ className, ...props }: FormProps) => (
  <form className={joinClassNames(b(), className)} {...props} />
)
