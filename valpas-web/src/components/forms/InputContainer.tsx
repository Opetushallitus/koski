import bem from "bem-ts"
import React from "react"

export type Props = {
  bemBase: string
  children: React.ReactNode
  label?: string
  icon?: React.ReactNode
}

export const InputContainer = (props: Props) => {
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
    </label>
  )
}
