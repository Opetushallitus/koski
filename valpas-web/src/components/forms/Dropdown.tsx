import bem from "bem-ts"
import React from "react"
import { FilterableValue, toFilterableString } from "../../utils/conversions"
import { ArrowDropDownIcon } from "../icons/Icon"
import "./Dropdown.less"
import { InputContainer } from "./InputContainer"

const b = bem("dropdown")

export type Props<T> = {
  options: DropdownOption<T>[]
  value: T
  onChange: (value?: T) => void
  label?: string
  icon?: React.ReactNode
}

export type DropdownOption<T> = {
  value?: T
  display: string
}

export const Dropdown = <T,>(props: Props<T>) => (
  <InputContainer
    bemBase="dropdown"
    label={props.label}
    icon={props.icon || <ArrowDropDownIcon />}
  >
    <select
      className={b("input")}
      value={props.options.findIndex((opt) => opt.value === props.value)}
      onChange={(event) =>
        props.onChange(props.options[parseInt(event.target.value, 10)]?.value)
      }
    >
      {props.options.map((option, index) => (
        <option key={index} value={index}>
          {option.display}
        </option>
      ))}
    </select>
  </InputContainer>
)

export const listToOptions = <T extends FilterableValue>(
  list: T[]
): Array<DropdownOption<T>> =>
  list.map((item) => ({
    value: item,
    display: toFilterableString(item),
  }))
