import React, { useCallback, useEffect, useRef, useState } from 'react'
import { parseFinnishDate, formatFinnishDate } from './date'
import DayPicker, { DateUtils } from 'react-day-picker'
import { t } from '../i18n/i18n'
import { HiddenDescription } from '../components/HiddenDescription'

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

const weekdaysShort = ['Su', 'Ma', 'Ti', 'Ke', 'To', 'Pe', 'La'].map((v) =>
  t(v)
)

const DateInput = (props) => {
  const {
    isAllowedDate = () => true,
    validityCallback = () => {},
    valueCallback = () => {},
    optional = false,
    inputId = 'date-input',
    isPending,
    ...rest // aria-label jne.
  } = props
  const [value, setValue] = useState(() =>
    props.value ? formatFinnishDate(props.value) : ''
  )
  const [invalidDate, setInvalidDate] = useState()
  const [calendarOpen, setCalendarOpen] = useState()
  const calendarInputRef = useRef(null)

  const handleClickOutside = useCallback((event) => {
    if (!calendarInputRef.current?.contains(event.target)) {
      document.removeEventListener('click', handleClickOutside, false)
      setCalendarOpen(false)
    }
  }, [])

  useEffect(() => {
    return () => {
      document.removeEventListener('click', handleClickOutside, false)
    }
  }, [handleClickOutside])

  const toggleCalendarOpen = (e) => {
    e.preventDefault()
    const open = !calendarOpen
    if (open) {
      document.addEventListener('click', handleClickOutside, false)
    }
    setCalendarOpen(open)
  }

  const onChange = (event) => {
    const stringInput = event.target.value
    setValue(stringInput)
    const date = parseFinnishDate(stringInput)
    const valid = (optional && !stringInput) || (date && isAllowedDate(date))
    handleDaySelection(date, valid, stringInput)
  }

  const handleDayClick = (date, { disabled }) => {
    if (disabled) {
      return
    }
    handleDaySelection(date, true)
    setCalendarOpen(false)
    setValue(formatFinnishDate(date))
  }

  const handleDaySelection = (date, valid, stringInput) => {
    if (valid) {
      valueCallback(date)
    }
    validityCallback(valid, stringInput)
    setInvalidDate(!valid)
  }

  return (
    <div className="calendar-input" ref={calendarInputRef} {...rest}>
      <HiddenDescription id={'aria-description:date-input'} />
      <input
        type="text"
        id={inputId}
        value={value || ''}
        onChange={onChange}
        className={
          invalidDate
            ? 'editor-input date-editor error'
            : 'editor-input date-editor'
        }
        aria-invalid={invalidDate}
        aria-describedby="aria-description:date-input"
      />
      <a className="toggle-calendar" onClick={toggleCalendarOpen}>
        {''}
      </a>
      {calendarOpen && (
        <div className="date-picker-wrapper">
          <div className="date-picker-overlay">
            <DayPicker
              initialMonth={parseFinnishDate(value)}
              onDayClick={handleDayClick}
              selectedDays={(day) =>
                DateUtils.isSameDay(parseFinnishDate(value), day)
              }
              weekdaysShort={weekdaysShort}
              months={months}
              firstDayOfWeek={1}
              disabledDays={(day) => !isAllowedDate(day)}
            />
          </div>
        </div>
      )}
    </div>
  )
}

export default DateInput
