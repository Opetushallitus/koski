import React, { useState } from "react"
import { Dropdown, listToOptions } from "../forms/Dropdown"
import { TextField } from "../forms/TextField"
import { SearchIcon } from "../icons/Icon"

export type FilterFn = (input: string) => boolean

type FilterProps = {
  values: string[]
  onChange: (filterFn: FilterFn | null) => void
}

export type Props = FilterProps & {
  type: DataFilter
}

export type DataFilter = "freetext" | "dropdown"
export const dataFilterUsesValueList = (type?: DataFilter): boolean =>
  type === "dropdown"

export const DataTableFilter = ({ type, ...props }: Props) => {
  switch (type) {
    case "freetext":
      return <FreeTextFilter {...props} />
    case "dropdown":
      return <SelectFilter {...props} />
    default:
      return null
  }
}

const FreeTextFilter = (props: FilterProps) => {
  const [needle, setNeedle] = useState("")
  return (
    <TextField
      value={needle}
      onChange={(value) => {
        setNeedle(value)
        props.onChange(value !== "" ? createFreeTextFilter(value) : null)
      }}
      icon={<SearchIcon />}
    />
  )
}

const createFreeTextFilter: (needle: string) => FilterFn = (needle) => {
  const lcNeedle = needle.toLowerCase()
  return (input) => input.toLowerCase().includes(lcNeedle)
}

const SelectFilter = (props: FilterProps) => {
  const [value, setValue] = useState<string | undefined>(undefined)
  return (
    <Dropdown
      value={value}
      options={[
        {
          value: undefined,
          display: "",
        },
        ...listToOptions(props.values),
      ]}
      onChange={(value) => {
        setValue(value)
        props.onChange(value ? createDropdownFilter(value) : null)
      }}
    />
  )
}

const createDropdownFilter: (value: string) => FilterFn = (value) => (input) =>
  value === input
