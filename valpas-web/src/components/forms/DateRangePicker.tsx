import bem from "bem-ts"
import React, { useCallback } from "react"
import { ISODate } from "../../state/common"
import { isValidSortedValues } from "../../utils/date"
import { DatePicker } from "./DatePicker"
import "./DateRangePicker.less"

const b = bem("daterangepicker")

export type DateRange = [ISODate | null, ISODate | null]

export type DateRangePickerProps = {
  value: DateRange
  onChange: (range: DateRange) => void
  disabled?: boolean
}

export const DateRangePicker = (props: DateRangePickerProps) => {
  const values = sortDateRange(props.value)
  const setValuesSorted = useCallback(
    (range: DateRange) => {
      const [value1, value2] = range
      if (isValidSortedValues(value1, value2)) {
        props.onChange(sortDateRange([value1, value2]))
      }
    },
    [props]
  )

  return (
    <div className={b()}>
      <DatePicker
        value={values[0]!}
        onChange={(date) => setValuesSorted([date, values[1]!])}
        disabled={props.disabled}
      />
      <span className={b("separator")}>{" — "}</span>
      <DatePicker
        value={values[1]!}
        onChange={(date) => setValuesSorted([values[0]!, date])}
        disabled={props.disabled}
      />
    </div>
  )
}

const sortDateRange = (range: DateRange): DateRange =>
  range[0] && range[1] && range[1] < range[0] ? [range[1], range[0]] : range
