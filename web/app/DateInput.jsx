import React from 'react'
import {parseFinnishDate, formatFinnishDate} from './date.js'
import DayPicker, {DateUtils} from 'react-day-picker'

const months = ['Tammikuu', 'Helmikuu', 'Maaliskuu', 'Huhtikuu', 'Toukokuu',
  'Kesäkuu', 'Heinäkuu', 'Elokuu', 'Syyskuu', 'Lokakuu', 'Marraskuu',
  'Joulukuu']

const weekdaysShort = ['Su', 'Ma', 'Ti', 'Ke', 'To', 'Pe', 'La']

export default React.createClass({
  render() {
    let {isAllowedDate = () => true, validityCallback = () => {}, valueCallback = () => {}, optional = false } = this.props
    let {invalidDate} = this.state

    let toggleCalendarOpen = (e) => {
      e.preventDefault()
      let open = !this.state.calendarOpen
      if(open) {
        document.addEventListener('click', this.handleClickOutside, false)
      }
      this.setState({ calendarOpen: open })
    }

    let onChange = (event) => {
      this.setState({value: event.target.value})
      let date = parseFinnishDate(event.target.value)
      let valid = (optional && !event.target.value) || (date && isAllowedDate(date))
      handleDaySelection(date, valid)
    }

    let handleDayClick = (e, date, { disabled }) => {
      if (disabled) {
        return
      }
      handleDaySelection(date, true)
      this.setState({
        calendarOpen: false,
        value: formatFinnishDate(date)
      })
    }

    let handleDaySelection = (date, valid) => {
      if (valid) {
        valueCallback(date)
      }
      validityCallback(valid)
      this.setState({invalidDate: !valid})
    }

    return (
      <div className="calendar-input" ref={input => this.calendarInput = input}>
        <input type="text" value={this.state.value} onChange={ onChange } className={invalidDate ? 'date-editor error' : 'date-editor'} />
        <a className="toggle-calendar" onClick={toggleCalendarOpen}></a>
        { this.state.calendarOpen &&
        <div className="date-picker-wrapper">
          <div className="date-picker-overlay">
            <DayPicker
              initialMonth={ parseFinnishDate(this.state.value) }
              onDayClick={ handleDayClick }
              selectedDays={ day => DateUtils.isSameDay(parseFinnishDate(this.state.value), day) }
              weekdaysShort={weekdaysShort}
              months={months}
              firstDayOfWeek={ 1 }
              disabledDays={day => !isAllowedDate(day)}
            />
          </div>
        </div>
        }
      </div>
    )
  },
  removeListeners() {
    document.removeEventListener('click', this.handleClickOutside, false)
  },
  getInitialState() {
    return {value: formatFinnishDate(this.props.value)}
  },
  handleClickOutside(e) {
    if(!(this.calendarInput && this.calendarInput.contains(e.target))) {
      this.removeListeners()
      this.setState({calendarOpen: false})
    }
  },
  componentWillUnmount() {
    this.removeListeners()
  }
})