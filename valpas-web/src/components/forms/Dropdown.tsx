import bem from "bem-ts"
import * as A from "fp-ts/Array"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import React from "react"
import { getLocalizedMaybe } from "../../i18n/i18n"
import { KoodistoKoodiviite } from "../../state/apitypes/koodistot"
import {
  Organisaatio,
  organisaatioNimi,
} from "../../state/apitypes/organisaatiot"
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
  sort?: Ord.Ord<DropdownOption<T>>
  testId?: string
  disabled?: boolean
}

export type DropdownOption<T> = {
  value?: T
  display: string
}

export const Dropdown = <T,>(props: DropdownProps<T>) => {
  const showEmptyValue = !props.options.some(
    (option) => option.value === props.value,
  )
  const sortedOptions = props.sort
    ? A.sort(props.sort)(props.options)
    : props.options
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
        value={sortedOptions.findIndex((opt) => opt.value === props.value)}
        onChange={(event) =>
          props.onChange(sortedOptions[parseInt(event.target.value, 10)]?.value)
        }
        onBlur={props.onBlur}
        data-testid={props.testId}
        disabled={props.disabled || false}
      >
        {showEmptyValue ? <option>-</option> : null}
        {sortedOptions.map((option, index) => (
          <option key={index} value={index}>
            {option.display}
          </option>
        ))}
      </select>
    </InputContainer>
  )
}

export const listToOptions = <T extends FilterableValue>(
  list: T[],
): Array<DropdownOption<T>> =>
  list.map((item) => ({
    value: item,
    display: toFilterableString(item),
  }))

export const koodistoToOptions = (
  koodiviitteet: KoodistoKoodiviite[],
): Array<DropdownOption<string>> =>
  koodiviitteet.map((koodiviite) => ({
    value: koodiviite.koodiarvo,
    display: getLocalizedMaybe(koodiviite.nimi) || koodiviite.koodiarvo,
  }))

export const organisaatiotToOptions = (
  organisaatiot: Organisaatio[],
): Array<DropdownOption<string>> =>
  organisaatiot.map((org) => ({
    value: org.oid,
    display: organisaatioNimi(org),
  }))

export const displayOrd = Ord.contramap((a: DropdownOption<any>) => a.display)(
  string.Ord,
)
