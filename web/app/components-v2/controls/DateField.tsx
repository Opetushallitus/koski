import React, { useCallback, useMemo, useState } from 'react'
import DayPickerInput, { DateUtils } from 'react-day-picker'
import {
  formatFinnishDate,
  formatISODate,
  ISO2FinnishDate,
  parseFinnishDate
} from '../../date/date'
import { t } from '../../i18n/i18n'
import { common, CommonProps, cx } from '../CommonProps'
import {
  PositionalPopup,
  PositionalPopupHolder
} from '../containers/PositionalPopup'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'
import { invalidDate } from '../forms/validator'
import { IconButton } from './IconButton'

// Date viewer

export type DateViewProps = CommonProps<FieldViewBaseProps<string>>

export const DateView: React.FC<DateViewProps> = (props) => {
  const formattedDate = useMemo(
    () => (props.value ? ISO2FinnishDate(props.value) : '–'),
    [props.value]
  )
  return <span {...common(props, ['DateView'])}>{formattedDate}</span>
}

// Date editor

export type DateEditProps = CommonProps<
  FieldEditBaseProps<
    string,
    {
      min?: string
      max?: string
    }
  >
>

export const DateEdit: React.FC<DateEditProps> = (props) => {
  const {
    date,
    displayDate,
    datePickerVisible,
    isInvalidDate,
    hasError,
    selectedDays,
    toggleDayPicker,
    onDayClick,
    onChange
  } = useDateEditState(props)

  return (
    <label {...common(props, ['DateEdit'])}>
      <div className="DateEdit__field">
        <input
          type="text"
          value={displayDate}
          onChange={onChange}
          className={cx(
            'DateEdit__input',
            hasError && 'DateEdit__input--error'
          )}
        />
        <PositionalPopupHolder>
          <IconButton
            charCode="f133"
            label={t('Valitse päivämäärä')}
            size="input"
            onClick={toggleDayPicker}
          />
          {datePickerVisible && (
            <PositionalPopup>
              <DayPickerInput
                initialMonth={date}
                onDayClick={onDayClick}
                selectedDays={selectedDays}
                weekdaysShort={weekdaysShort}
                months={months}
                firstDayOfWeek={1}
              />
            </PositionalPopup>
          )}
        </PositionalPopupHolder>
      </div>
      <FieldErrors
        errors={props.errors}
        localErrors={
          isInvalidDate ? [invalidDate(displayDate || '', [])] : undefined
        }
      />
    </label>
  )
}

// Utils

const useDateEditState = (props: DateEditProps) => {
  const [datePickerVisible, setDatePickerVisible] = useState(false)
  const toggleDayPicker = useCallback(
    () => setDatePickerVisible(!datePickerVisible),
    [datePickerVisible]
  )

  const finnishDate = useMemo(() => ISO2FinnishDate(props.value), [props.value])
  const [internalFinnishDate, setInternalFinnishDate] = useState(finnishDate)
  const internalDate = useMemo(
    () =>
      internalFinnishDate ? parseFinnishDate(internalFinnishDate) : undefined,
    [internalFinnishDate]
  )
  const [isInvalidDate, setInvalidDate] = useState(false)

  const { onChange, min, max, value } = props
  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      const newFinnishDate = event.target.value
      setInternalFinnishDate(newFinnishDate)
      const date = parseFinnishDate(newFinnishDate)
      const isoDate = date && formatISODate(date)
      setInvalidDate(!isoDate)
      if (isoDate && isoDate !== value) {
        if ((min && isoDate < min) || (max && isoDate > max)) {
          setInvalidDate(true)
        }
        onChange(isoDate)
      }
    },
    [onChange, min, max, value]
  )

  const onDayClick = useCallback(
    (date: Date) => {
      setInternalFinnishDate(formatFinnishDate(date))
      setDatePickerVisible(false)
      const isoDate = formatISODate(date)
      if (isoDate && isoDate !== value) {
        onChange(isoDate)
      }
    },
    [onChange, value]
  )

  const selectedDays = useCallback(
    (date: Date) =>
      internalDate ? DateUtils.isSameDay(date, internalDate) : false,
    [internalDate]
  )

  return {
    date: internalDate,
    displayDate: internalFinnishDate,
    datePickerVisible,
    isInvalidDate,
    hasError: Boolean(isInvalidDate || props.errors),
    selectedDays,
    toggleDayPicker,
    onDayClick,
    onChange: onChangeCB
  }
}

const weekdaysShort = ['Su', 'Ma', 'Ti', 'Ke', 'To', 'Pe', 'La'].map((v) =>
  t(v)
)

const months = [
  'Tammikuu',
  'Helmikuu',
  'Maaliskuu',
  'Huhtikuu',
  'Toukokuu',
  'Kesäkuu',
  'Heinäkuu',
  'Elokuu',
  'Syyskuu',
  'Lokakuu',
  'Marraskuu',
  'Joulukuu'
].map((v) => t(v))

const CalendarButton: React.FC<{
  onClick: React.MouseEventHandler<HTMLAnchorElement>
}> = (props) => (
  <a className="DateEdit__pickerBtn" onClick={props.onClick}>
    {''}
  </a>
)
