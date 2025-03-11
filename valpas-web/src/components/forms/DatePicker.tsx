import bem from "bem-ts"
import { formatISO, parseISO } from "date-fns"
import React, { useCallback, useMemo } from "react"
import ReactDatePicker from "react-date-picker"
import { getLanguage } from "../../i18n/i18n"
import { ISODate } from "../../state/common"

import "react-date-picker/dist/DatePicker.css"
import "react-calendar/dist/Calendar.css"
import "./DatePicker.less"
import { Value } from "react-calendar/dist/esm/shared/types.js"

const b = bem("datepicker")

export type DatePickerProps = {
  value: ISODate | null
  onChange: (date: ISODate | null) => void
  disabled?: boolean
}

export const DatePicker = (props: DatePickerProps) => {
  const selected = useMemo(
    () => (props.value !== null ? parseISO(props.value) : null),
    [props.value],
  )

  const emitChange = props.onChange
  const onChange = useCallback(
    (date: Value) =>
      date instanceof Date
        ? emitChange(formatISO(date, { representation: "date" }))
        : console.error("Unsupported date value:", date),
    [emitChange],
  )

  return (
    <ReactDatePicker
      className={b("input")}
      value={selected}
      onChange={onChange}
      locale={getLanguage()}
      clearIcon={null}
      disabled={props.disabled}
    />
  )
}
