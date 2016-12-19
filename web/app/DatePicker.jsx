import DayPicker, {DateUtils} from 'react-day-picker'
import {parseFinnishDate, formatFinnishDate} from './date.js'
import React from 'react'

export default React.createClass({
  render() {
    const {from, to, displayedStartMonth, displayedEndMonth} = this.state
    return (
      <div className="calendar">
        <div onClick={e => this.openCalendar(e)}>{ from && to ? (formatFinnishDate(from) + '-' + formatFinnishDate(to)) : 'kaikki'}</div>
        { this.state.open &&
          <div className="DayPicker-CalendarContainer">
              <DayPicker
                onDayClick={ this.handleDayClick }
                selectedDays={ day => DateUtils.isDayInRange(day, { from, to }) }
                locale="fi"
                localeUtils={localeUtils}
                initialMonth={displayedStartMonth}
                fixedWeeks
                onMonthChange={ this.handleStartMonthChange}
                toMonth={ DateUtils.addMonths(displayedEndMonth, -1) }
              />
              <DayPicker
                onDayClick={ this.handleDayClick }
                selectedDays={ day => DateUtils.isDayInRange(day, { from, to }) }
                locale="fi"
                localeUtils={localeUtils}
                fromMonth={ DateUtils.addMonths(displayedStartMonth, 1)}
                onMonthChange={ this.handleEndMonthChange}
                fixedWeeks
              />
          </div>
        }
      </div>
    )
  },
  handleStartMonthChange(day) {
    this.setState({
      displayedStartMonth: day
    })
  },
  handleEndMonthChange(day) {
    this.setState({
      displayedEndMonth: day
    })
  },
  handleDayClick(e, day) {
    this.setState(DateUtils.addDayToRange(day, this.state))
  },
  getInitialState() {
    return {
      displayedStartMonth: new Date(new Date().getFullYear(), 0, 1),
      displayedEndMonth: new Date(),
      open: false
    }
  },
  openCalendar(e) {
    this.setState({open: !this.state.open})
  },
  closeCalendar() {
    console.log('close calendar')
    this.setState({open: false})
  },
  componentDidMount() {
    window.addEventListener('click', this.handleClickOutside, false)
  },
  componentWillUnmount() {
    window.removeEventListener('click', this.handleClickOutside, false)
  },
  handleClickOutside(e) {
    console.log('clicketi outside', !e.target.closest('.calendar'))
    !e.target.closest('.calendar') && this.closeCalendar()
  }
  /*,
  handleFocus(e) {
    console.log("focus")
  },
  handleBlur(e) {
    console.log("blur", e)
  }*/
  /*,
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
  }*/
})

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