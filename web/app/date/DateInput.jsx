import React from 'react'
import { parseFinnishDate, formatFinnishDate } from './date'
import DayPicker, { DateUtils } from 'react-day-picker'
import { t } from '../i18n/i18n'
import PropTypes from 'prop-types'
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

class DateInput extends React.Component {
  constructor(props) {
    super(props)
    const value = props.value
    this.state = { value: value ? formatFinnishDate(value) : '' }
    this.handleClickOutside = this.handleClickOutside.bind(this)
    this.removeListeners = this.removeListeners.bind(this)
  }

  render() {
    const {
      isAllowedDate = () => true,
      validityCallback = () => {},
      valueCallback = () => {},
      optional = false,
      inputId = 'date-input',
      isPending,
      ...rest // aria-label jne.
    } = this.props
    const { invalidDate } = this.state

    const toggleCalendarOpen = (e) => {
      e.preventDefault()
      const open = !this.state.calendarOpen
      if (open) {
        document.addEventListener('click', this.handleClickOutside, false)
      }
      this.setState({ calendarOpen: open })
    }

    const onChange = (event) => {
      const stringInput = event.target.value
      this.setState({ value: stringInput })
      const date = parseFinnishDate(stringInput)
      const valid = (optional && !stringInput) || (date && isAllowedDate(date))
      handleDaySelection(date, valid, stringInput)
    }

    const handleDayClick = (date, { disabled }) => {
      if (disabled) {
        return
      }
      handleDaySelection(date, true)
      this.setState({
        calendarOpen: false,
        value: formatFinnishDate(date)
      })
    }

    const handleDaySelection = (date, valid, stringInput) => {
      if (valid) {
        valueCallback(date)
      }
      validityCallback(valid, stringInput)
      this.setState({ invalidDate: !valid })
    }

    return (
      <div
        className="calendar-input"
        ref={(input) => (this.calendarInput = input)}
        {...rest}
      >
        <HiddenDescription id={'aria-description:date-input'} />
        <input
          type="text"
          id={inputId}
          value={this.state.value || ''}
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
        {this.state.calendarOpen && (
          <div className="date-picker-wrapper">
            <div className="date-picker-overlay">
              <DayPicker
                initialMonth={parseFinnishDate(this.state.value)}
                onDayClick={handleDayClick}
                selectedDays={(day) =>
                  DateUtils.isSameDay(parseFinnishDate(this.state.value), day)
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

  removeListeners() {
    document.removeEventListener('click', this.handleClickOutside, false)
  }

  handleClickOutside(e) {
    if (!(this.calendarInput && this.calendarInput.contains(e.target))) {
      this.removeListeners()
      this.setState({ calendarOpen: false })
    }
  }

  componentWillUnmount() {
    this.removeListeners()
  }
}

DateInput.propTypes = {
  isAllowedDate: PropTypes.func,
  isPending: PropTypes.bool,
  valueCallback: PropTypes.func,
  validityCallback: PropTypes.func
}
export default DateInput
