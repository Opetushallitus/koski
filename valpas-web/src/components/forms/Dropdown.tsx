import bem from "bem-ts"
import React from "react"
import { getLocalized } from "../../i18n/i18n"
import { KoodistoKoodiviite } from "../../state/apitypes/koodistot"
import { FilterableValue, toFilterableString } from "../../utils/conversions"
import { ArrowDropDownIcon } from "../icons/Icon"
import "./Dropdown.less"
import { InputContainer } from "./InputContainer"

const b = bem("dropdown")

export type DropdownProps<T> = {
  options: DropdownOption<T>[]
  value?: T
  onChange: (value?: T) => void
  onBlur?: () => void
  label?: string
  icon?: React.ReactNode
  error?: React.ReactNode
  selectorId?: string
  containerClassName?: string
  required?: boolean
}

export type DropdownOption<T> = {
  value?: T
  display: string
}

export const Dropdown = <T,>(props: DropdownProps<T>) => {
  const showEmptyValue = !props.options.some(
    (option) => option.value === props.value
  )
  return (
    <InputContainer
      className={props.containerClassName}
      bemBase="dropdown"
      label={props.label}
      icon={props.icon || <ArrowDropDownIcon />}
      error={props.error}
      required={props.required}
    >
      <select
        id={props.selectorId}
        className={b("input", { error: Boolean(props.error) })}
        value={props.options.findIndex((opt) => opt.value === props.value)}
        onChange={(event) =>
          props.onChange(props.options[parseInt(event.target.value, 10)]?.value)
        }
        onBlur={props.onBlur}
      >
        {showEmptyValue ? <option>-</option> : null}
        {props.options.map((option, index) => (
          <option key={index} value={index}>
            {option.display}
          </option>
        ))}
      </select>
    </InputContainer>
  )
}

export const listToOptions = <T extends FilterableValue>(
  list: T[]
): Array<DropdownOption<T>> =>
  list.map((item) => ({
    value: item,
    display: toFilterableString(item),
  }))

export const koodistoToOptions = (
  koodiviitteet: KoodistoKoodiviite[]
): Array<DropdownOption<string>> =>
  koodiviitteet.map((koodiviite) => ({
    value: koodiviite.koodiarvo,
    display: getLocalized(koodiviite.nimi) || koodiviite.koodiarvo,
  }))
