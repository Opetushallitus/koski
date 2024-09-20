import React, { useCallback, useEffect, useMemo, useState } from 'react'
import DayPickerInput, { DateUtils } from 'react-day-picker'
import { useTestId } from '../../appstate/useTestId'
import {
  formatFinnishDate,
  formatISODate,
  ISO2FinnishDate,
  parseFinnishDate
} from '../../date/date'
import { t } from '../../i18n/i18n'
import { EmptyObject } from '../../util/objects'
import { common, CommonProps, cx } from '../CommonProps'
import {
  PositionalPopup,
  PositionalPopupHolder
} from '../containers/PositionalPopup'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { IconButton } from './IconButton'

// Date viewer

export type DateViewProps = CommonProps<FieldViewerProps<string, EmptyObject>>

export const DateView: React.FC<DateViewProps> = (props) => {
  const testId = useTestId('date.value')
  const formattedDate = useMemo(
    () => (props.value ? ISO2FinnishDate(props.value) : '–'),
    [props.value]
  )

  return (
    <span {...common(props, ['DateView'])} data-testid={testId}>
      {formattedDate}
    </span>
  )
}

// Date editor

export type DateEditProps = CommonProps<
  FieldEditorProps<
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
    hasError,
    selectedDays,
    toggleDayPicker,
    onDayClick,
    onChange,
    inputKey
  } = useDateEditState(props)
  const testId = props.testId || 'date'
  const inputId = useTestId(`${testId}.edit.input`)
  const buttonId = useTestId(`${testId}.edit.calendarButton`)

  return (
    <label {...common(props, ['DateEdit'])}>
      <div className="DateEdit__field">
        <input
          type="text"
          defaultValue={displayDate}
          onChange={onChange}
          className={cx(
            'DateEdit__input',
            hasError && 'DateEdit__input--error'
          )}
          data-testid={inputId}
          key={inputKey}
        />
        <PositionalPopupHolder>
          <IconButton
            charCode="f133"
            label={t('Valitse päivämäärä')}
            size="input"
            onClick={toggleDayPicker}
            data-testid={buttonId}
          />
          <PositionalPopup open={datePickerVisible}>
            <DayPickerInput
              initialMonth={date}
              onDayClick={onDayClick}
              selectedDays={selectedDays}
              weekdaysShort={weekdaysShort}
              months={months}
              firstDayOfWeek={1}
            />
          </PositionalPopup>
        </PositionalPopupHolder>
      </div>
      <FieldErrors errors={props.errors} />
    </label>
  )
}

// Utils

const useDateEditState = (props: DateEditProps) => {
  const [inputKey, setInputKey] = useState('init')

  const [datePickerVisible, setDatePickerVisible] = useState(false)
  const toggleDayPicker = useCallback(
    () => setDatePickerVisible(!datePickerVisible),
    [datePickerVisible]
  )

  const finnishDate = useMemo(() => ISO2FinnishDate(props.value), [props.value])
  const [internalFinnishDate, setInternalFinnishDate] = useState(finnishDate)
  useEffect(() => {
    if (finnishDate !== internalFinnishDate) {
      setInternalFinnishDate(finnishDate)
    }
  }, [finnishDate, internalFinnishDate])
  const internalDate = useMemo(
    () =>
      internalFinnishDate ? parseFinnishDate(internalFinnishDate) : undefined,
    [internalFinnishDate]
  )

  const { onChange, min: _min, max: _max, value } = props
  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      const newFinnishDate = event.target.value
      setInternalFinnishDate(newFinnishDate)
      const date = parseFinnishDate(newFinnishDate)
      const isoDate = date && formatISODate(date)
      onChange(isoDate)
    },
    [onChange]
  )

  const onDayClick = useCallback(
    (date: Date) => {
      setInternalFinnishDate(formatFinnishDate(date))
      setDatePickerVisible(false)
      const isoDate = formatISODate(date)
      if (isoDate && isoDate !== value) {
        setInputKey(`update-${new Date().getTime()}`)
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
    hasError: Boolean(props.errors),
    selectedDays,
    toggleDayPicker,
    onDayClick,
    onChange: onChangeCB,
    inputKey
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
