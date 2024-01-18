import React, { useState } from "react"
import { t } from "../../i18n/i18n"
import { Dropdown, listToOptions } from "../forms/Dropdown"
import { TextField } from "../forms/TextField"
import { SearchIcon } from "../icons/Icon"

export type FilterFn = (input: string) => boolean

type FilterProps = {
  values: string[]
  initialValue: string | null | undefined
  onChange: (filterFn: FilterFn | null, value: string | null) => void
}

export type DataTableFilterProps = FilterProps & {
  type: DataFilter
}

export type DataFilter = "freetext" | "dropdown"
export const dataFilterUsesValueList = (type?: DataFilter): boolean =>
  type === "dropdown"

export const DataTableFilter = ({ type, ...props }: DataTableFilterProps) => {
  switch (type) {
    case "freetext":
      return <FreeTextFilter {...props} />
    case "dropdown":
      return <SelectFilter {...props} />
    default:
      return null
  }
}

export const createFilter = (
  type?: DataFilter,
  needle?: string | null,
): FilterFn | null => {
  if (!needle || !type) {
    return null
  }
  switch (type) {
    case "freetext":
      return createFreeTextFilter(needle)
    case "dropdown":
      return createDropdownFilter(needle)
    default:
      throw new Error(`Unknown filter type: ${type}`)
  }
}

const FreeTextFilter = (props: FilterProps) => {
  const [needle, setNeedle] = useState(props.initialValue || "")
  const emit = (value: string) => {
    setNeedle(value)
    props.onChange(value !== "" ? createFreeTextFilter(value) : null, value)
  }
  return <TextField value={needle} onChange={emit} icon={<SearchIcon />} />
}

const createFreeTextFilter: (needle: string) => FilterFn = (needle) => {
  const lcNeedle = needle.toLowerCase()
  return (input) => input.toLowerCase().includes(lcNeedle)
}

const SelectFilter = (props: FilterProps) => {
  const [value, setValue] = useState<string | undefined>(
    props.initialValue || undefined,
  )
  const emit = (value: string | undefined) => {
    setValue(value)
    props.onChange(value ? createDropdownFilter(value) : null, value || null)
  }

  return (
    <Dropdown
      value={value}
      options={[
        {
          value: undefined,
          display: t("taulukko__kaikki"),
        },
        ...listToOptions(props.values),
      ]}
      onChange={emit}
    />
  )
}

const createDropdownFilter: (value: string) => FilterFn = (value) => (input) =>
  value === input
