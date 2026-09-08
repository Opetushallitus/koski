import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
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
  PositionalPopupAlign,
  PositionalPopupHolder
} from '../containers/PositionalPopup'
import { FieldErrors } from '../forms/FieldErrors'
import {
  FieldEditorProps,
  FieldViewerProps,
  componentsWithBuiltInErrors
} from '../forms/FormField'
import { IconButton } from './IconButton'

// Date viewer

export type DateViewProps = CommonProps<FieldViewerProps<string, EmptyObject>>

export const DateView: React.FC<DateViewProps> = (props) => {
  const testId = useTestId(props.testId ? props.testId : 'date.value')
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
> & {
  align?: PositionalPopupAlign
}

export const DateEdit: React.FC<DateEditProps> = (props) => {
  const {
    date,
    displayDate,
    datePickerVisible,
    hasError,
    selectedDays,
    toggleDayPicker,
    onDayClick,
    onChange
  } = useDateEditState(props)
  const testId = props.testId || 'date'
  const inputId = useTestId(`${testId}.edit.input`)
  const buttonId = useTestId(`${testId}.edit.calendarButton`)

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
          data-testid={inputId}
        />
        <PositionalPopupHolder>
          <IconButton
            charCode="f133"
            label={t('Valitse päivämäärä')}
            size="input"
            onClick={toggleDayPicker}
            data-testid={buttonId}
          />
          <PositionalPopup align={props.align} open={datePickerVisible}>
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
componentsWithBuiltInErrors.add(DateEdit)

// Utils

const useDateEditState = (props: DateEditProps) => {
  const [datePickerVisible, setDatePickerVisible] = useState(false)
  const toggleDayPicker = useCallback(
    () => setDatePickerVisible(!datePickerVisible),
    [datePickerVisible]
  )

  // Kentän teksti pidetään omassa tilassaan, koska kirjoittamisen aikana se on
  // väliaikaisesti kelvoton ("1.1.201") eikä sitä voi johtaa propsin arvosta.
  const [internalFinnishDate, setInternalFinnishDate] = useState(
    () => ISO2FinnishDate(props.value) || ''
  )
  const internalFinnishDateRef = useRef(internalFinnishDate)
  internalFinnishDateRef.current = internalFinnishDate

  const { onChange, min: _min, max: _max, value } = props

  // Ulkopuolelta tullut arvo (esim. päätason suoritus vaihtui välilehteä
  // vaihtaessa tai uusi vuosiluokka lisättiin) pitää saada kenttään näkyviin.
  // Sitä ei tunnisteta tekstiä vertaamalla vaan vertaamalla päivää, jota kentän
  // teksti tarkoittaa: jos se on jo sama kuin propsin arvo, muutos on peräisin
  // kentästä itsestään eikä tekstiä saa korvata. Efektin ainoa riippuvuus on
  // props.value — kentän teksti luetaan referenssistä — jottei kirjoittaminen
  // pyyhi kesken jäänyttä syötettä.
  useEffect(() => {
    if (finnishDateToISO(internalFinnishDateRef.current) !== value) {
      setInternalFinnishDate(ISO2FinnishDate(value) || '')
    }
  }, [value])

  const internalDate = useMemo(
    () =>
      internalFinnishDate ? parseFinnishDate(internalFinnishDate) : undefined,
    [internalFinnishDate]
  )

  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      const newFinnishDate = event.target.value
      setInternalFinnishDate(newFinnishDate)
      onChange(finnishDateToISO(newFinnishDate))
    },
    [onChange]
  )

  const onDayClick = useCallback(
    (date: Date) => {
      setInternalFinnishDate(formatFinnishDate(date) || '')
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
    hasError: Boolean(props.errors),
    selectedDays,
    toggleDayPicker,
    onDayClick,
    onChange: onChangeCB
  }
}

/** Päivä, jota kenttään kirjoitettu teksti tarkoittaa, tai undefined jos teksti ei ole kelvollinen päivämäärä. */
const finnishDateToISO = (finnishDate: string): string | undefined => {
  const date = finnishDate ? parseFinnishDate(finnishDate) : undefined
  return date && formatISODate(date)
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
