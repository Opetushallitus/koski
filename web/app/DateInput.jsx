import React from 'react'
import {parseFinnishDate, formatFinnishDate} from './date.js'
import DayPicker, {DateUtils} from 'react-day-picker'
import {t} from './i18n'
import PropTypes from 'prop-types'

const months = ['Tammikuu', 'Helmikuu', 'Maaliskuu', 'Huhtikuu', 'Toukokuu',
  'Kesäkuu', 'Heinäkuu', 'Elokuu', 'Syyskuu', 'Lokakuu', 'Marraskuu',
  'Joulukuu'].map(t)

const weekdaysShort = ['Su', 'Ma', 'Ti', 'Ke', 'To', 'Pe', 'La'].map(t)

class DateInput extends React.Component {
  constructor(props) {
    super(props)
    let value = props.value
    this.state = {value: value ? formatFinnishDate(value) : ''}
  }

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
      var stringInput = event.target.value
      this.setState({value: stringInput})
      let date = parseFinnishDate(stringInput)
      let valid = (optional && !stringInput) || (date && isAllowedDate(date))
      handleDaySelection(date, valid, stringInput)
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

    let handleDaySelection = (date, valid, stringInput) => {
      if (valid) {
        valueCallback(date)
      }
      validityCallback(valid, stringInput)
      this.setState({invalidDate: !valid})
    }

    return (
      <div className="calendar-input" ref={input => this.calendarInput = input}>
        <input type="text" value={this.state.value || ''} onChange={ onChange } className={invalidDate ? 'editor-input date-editor error' : 'editor-input date-editor'} />
        <a className="toggle-calendar" onClick={toggleCalendarOpen}>{''}</a>
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
  }

  removeListeners() {
    document.removeEventListener('click', this.handleClickOutside, false)
  }

  handleClickOutside(e) {
    if(!(this.calendarInput && this.calendarInput.contains(e.target))) {
      this.removeListeners()
      this.setState({calendarOpen: false})
    }
  }

  componentWillUnmount() {
    this.removeListeners()
  }
}

DateInput.propTypes = {
  isAllowedDate: PropTypes.func,
  valueCallback: PropTypes.func,
  validityCallback: PropTypes.func
}
export default DateInput