import { ChangeEventHandler, useCallback, useMemo, useState } from 'react'
import { DateUtils } from 'react-day-picker'
import {
  finnishDate2ISO,
  formatFinnishDate,
  formatISODate,
  ISO2FinnishDate,
  parseFinnishDate
} from '../../date/date'
import { t } from '../../i18n/i18n'

export type DateEditStateProps = {
  value?: string
  onChange: (value?: string, rawValue?: string) => void
}

/**
 * Päivämääräkentän tila, jaettu DateEditin ja DateInputin kesken.
 *
 * Kentän teksti pidetään omassa tilassaan, koska kirjoittamisen aikana se on
 * väliaikaisesti kelvoton ("1.1.201") eikä sitä voi johtaa propsin arvosta.
 * Kenttä on hallittu (value, ei defaultValue): hallitsemattomana teksti jäisi
 * vanhentuneeksi, koska React asettaa kentän arvon vain kiinnitettäessä, mikä
 * nostaa selaimen dirty value -lipun, minkä jälkeen muuttunut defaultValue ei
 * enää päivitä näkyvää tekstiä.
 */
export const useDateEditState = ({ value, onChange }: DateEditStateProps) => {
  const [datePickerVisible, setDatePickerVisible] = useState(false)
  const toggleDayPicker = useCallback(
    () => setDatePickerVisible(!datePickerVisible),
    [datePickerVisible]
  )

  const [internalFinnishDate, setInternalFinnishDate] = useState(() =>
    displayValue(value)
  )

  // Ulkopuolelta tullut arvo (esim. päätason suoritus vaihtui tai listaan
  // lisättiin rivi, jolloin sama komponentti saa uuden omistajan) pitää saada
  // kenttään näkyviin. Muutos päätellään renderin aikana eikä efektissä, jotta
  // vanhentunut arvo ei ehdi näkyä ruudulla. Tekstiä ei korvata, jos se jo
  // tarkoittaa samaa päivää kuin arvo: silloin muutos on peräisin kentästä
  // itsestään eikä kesken jäänyttä syötettä ("1.1.201") saa pyyhkiä.
  const [previousValue, setPreviousValue] = useState(value)
  if (value !== previousValue) {
    setPreviousValue(value)
    if (finnishDate2ISO(internalFinnishDate) !== value) {
      setInternalFinnishDate(displayValue(value))
    }
  }

  const internalDate = useMemo(
    () =>
      internalFinnishDate ? parseFinnishDate(internalFinnishDate) : undefined,
    [internalFinnishDate]
  )

  const onChangeCB: ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      const newFinnishDate = event.target.value
      setInternalFinnishDate(newFinnishDate)
      onChange(finnishDate2ISO(newFinnishDate), newFinnishDate)
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

  const dayPickerProps = useMemo(
    () => ({
      initialMonth: internalDate,
      onDayClick,
      selectedDays,
      weekdaysShort,
      months,
      firstDayOfWeek: 1
    }),
    [internalDate, onDayClick, selectedDays]
  )

  return {
    displayDate: internalFinnishDate,
    datePickerVisible,
    toggleDayPicker,
    onChange: onChangeCB,
    dayPickerProps
  }
}

/**
 * Arvo kentässä näytettävänä tekstinä. Arvo, jota ei osata jäsentää, näytetään
 * sellaisenaan, jottei tallennettu tieto katoa käyttäjän silmistä.
 */
const displayValue = (value?: string): string =>
  (value && (ISO2FinnishDate(value) || value)) || ''

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
