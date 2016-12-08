import DayPicker, {DateUtils} from 'react-day-picker'
import {parseFinnishDate, formatFinnishDate} from './date.js'
import React from 'react'

const weekdaysLong = {
  fi: ['Sunnuntai', 'Maanantai', 'Tiistai', 'Keskiviikko', 'Torstai', 'Perjantai', 'Lauantai'],
  en: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
}
const weekdaysShort = {
  fi: ['Su', 'Ma', 'Ti', 'Ke', 'To', 'Pe', 'La'],
  en: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa']
}
const months = {
  fi: ['Tammikuu', 'Helmikuu', 'Maaliskuu', 'Huhtikuu', 'Toukokuu', 'Kesäkuu', 'Heinäkuu', 'Elokuu', 'Syyskuu', 'Lokakuu', 'Marraskuu', 'Joulukuu'],
  en: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
}
const firstDayOfWeek = {
  fi: 1,
  en: 0
}

const localeUtils = {
  formatDay: (d, locale = 'en') => `${weekdaysLong[locale][d.getDay()]}, ${d.getDate()} ${months[locale][d.getMonth()]} ${d.getFullYear()}`,
  formatWeekdayShort: (index, locale = 'en') => weekdaysShort[locale][index],
  formatWeekdayLong: (index, locale = 'en') => weekdaysLong[locale][index],
  getFirstDayOfWeek: locale => firstDayOfWeek[locale],
  getMonths: locale => months[locale],
  formatMonthTitle: (d, locale) => `${months[locale][d.getMonth()]} ${d.getFullYear()}`
}

export default React.createClass({
  render() {
    return (
      <div onMouseDown={ this.handleContainerMouseDown }>
        <input type="text" ref={ (el) => { this.input = el } } placeholder="kaikki" value={ this.state.value } onChange={ this.handleInputChange } onFocus={ this.handleInputFocus } onBlur={ this.handleInputBlur }/>
        { this.state.showOverlay &&
        <div className="DayPicker-CalendarContainer">
          <div className="DayPicker-Overlay">
            <DayPicker ref={ (el) => { this.daypicker = el } } onDayClick={ this.handleDayClick } selectedDays={ day => DateUtils.isSameDay(this.state.selectedDay, day) } locale="fi" localeUtils={localeUtils}/>
          </div>
        </div>
        }
      </div>
    )
  },
  handleContainerMouseDown() {
    this.clickedInside = true
    this.clickTimeout = setTimeout(() => {
      this.clickedInside = false
    }, 0)
  },
  handleInputFocus() {
    this.setState({showOverlay: true},
      () => this.state.selectedDay && this.daypicker.showMonth(this.state.selectedDay)
    )
  },
  handleInputChange(e) {
    const {value} = e.target
    const parsed = parseFinnishDate(value)
    this.setState({selectedDay: parsed, value, showOverlay: false}, () => {
      if (parsed || !value) {
        this.props.onSelectionChanged(parsed || undefined)
        this.input.blur()
      }
    })
  },
  handleInputBlur() {
    const showOverlay = this.clickedInside

    this.setState({showOverlay})

    if (showOverlay) {
      this.input.focus()
    }
  },
  handleDayClick(e, day) {
    const value = formatFinnishDate(day)
    this.setState({
      value: value,
      selectedDay: day,
      showOverlay: false
    }, () => this.props.onSelectionChanged(day))
    this.input.blur()
  },
  getInitialState() {
    const initialValue = this.props.selectedDay
    return {
      showOverlay: false,
      value: initialValue || '',
      selectedDay: initialValue && parseFinnishDate(initialValue) || null
    }
  },
  componentWillUnmount() {
    clearTimeout(this.clickTimeout)
  }
})