import * as A from 'fp-ts/Array'
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

export type DateViewProps = CommonProps<FieldViewBaseProps<string>>

export const DateView: React.FC<DateViewProps> = (props) => {
  const formattedDate = useMemo(
    () => (props.value ? ISO2FinnishDate(props.value) : '–'),
    [props.value]
  )
  return <span {...common(props, ['DateView'])}>{formattedDate}</span>
}

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

  const onChange: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      const finnishDate = event.target.value
      setInternalFinnishDate(finnishDate)
      const date = parseFinnishDate(finnishDate)
      const isoDate = date && formatISODate(date)
      setInvalidDate(!isoDate)
      if (isoDate && isoDate !== props.value) {
        if (
          (props.min && isoDate < props.min) ||
          (props.max && isoDate > props.max)
        ) {
          setInvalidDate(true)
        }
        props.onChange(isoDate)
      }
    },
    [props.onChange, props.value, props.min, props.max]
  )

  const onDayClick = useCallback((date: Date) => {
    setInternalFinnishDate(formatFinnishDate(date))
    setDatePickerVisible(false)
    const isoDate = formatISODate(date)
    if (isoDate && isoDate !== props.value) {
      props.onChange(isoDate)
    }
  }, [])

  const selectedDays = useCallback(
    (date: Date) =>
      internalDate ? DateUtils.isSameDay(date, internalDate) : false,
    [internalDate]
  )

  const hasError = Boolean(isInvalidDate || props.errors)

  return (
    <label {...common(props, ['DateEdit'])}>
      <div className="DateEdit__field">
        <input
          type="text"
          value={internalFinnishDate}
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
                initialMonth={internalDate}
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
          isInvalidDate
            ? [invalidDate(internalFinnishDate || '', [])]
            : undefined
        }
      />
    </label>
  )
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
