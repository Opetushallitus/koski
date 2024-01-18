import bem from "bem-ts"
import { formatISO, parseISO } from "date-fns"
import React, { useCallback, useMemo } from "react"
import ReactDatePicker from "react-date-picker"
import { getLanguage } from "../../i18n/i18n"
import { ISODate } from "../../state/common"
import "./DatePicker.less"

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
    (date: Date) => emitChange(formatISO(date, { representation: "date" })),
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
