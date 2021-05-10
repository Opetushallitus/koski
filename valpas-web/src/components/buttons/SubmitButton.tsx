import React from "react"
import "./buttons.less"
import { raisedButtonClassName } from "./RaisedButton"

export type SubmitButtonProps = React.InputHTMLAttributes<HTMLInputElement>

export const SubmitButton = (props: SubmitButtonProps) => {
  const { disabled, onClick, ...rest } = props
  return (
    <input type="submit" className={raisedButtonClassName(props)} {...rest} />
  )
}
