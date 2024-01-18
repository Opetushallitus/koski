import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"

export type InputContainerProps = {
  bemBase: string
  children: React.ReactNode
  label?: React.ReactNode
  required?: boolean
  icon?: React.ReactNode
  error?: React.ReactNode
  className?: string
}

export const InputContainer = (props: InputContainerProps) => {
  const b = bem(props.bemBase)
  return (
    <label
      className={joinClassNames(
        b({ withicon: Boolean(props.icon) }),
        props.className,
      )}
    >
      {props.label && (
        <div className={b("label")}>
          {props.label}
          {props.required ? " *" : null}
        </div>
      )}
      <div className={b("inputcontainer")}>
        {props.children}
        {props.icon && <div className={b("icon")}>{props.icon}</div>}
      </div>
      {props.error && <div className={b("error")}>{props.error}</div>}
    </label>
  )
}
