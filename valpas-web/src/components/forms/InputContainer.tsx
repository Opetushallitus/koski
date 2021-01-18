import bem from "bem-ts"
import React from "react"

export type InputContainerProps = {
  bemBase: string
  children: React.ReactNode
  label?: React.ReactNode
  icon?: React.ReactNode
  error?: React.ReactNode
}

export const InputContainer = (props: InputContainerProps) => {
  const b = bem(props.bemBase)
  return (
    <label
      className={b({
        withicon: Boolean(props.icon),
      })}
    >
      {props.label && <div className={b("label")}>{props.label}</div>}
      <div className={b("inputcontainer")}>
        {props.children}
        {props.icon && <div className={b("icon")}>{props.icon}</div>}
      </div>
      {props.error && <div className={b("error")}>{props.error}</div>}
    </label>
  )
}
