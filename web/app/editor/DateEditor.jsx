import React from 'react'
import Bacon from 'baconjs'
import {resetOptionalModel} from './OptionalEditor.jsx'
import {modelTitle, modelSetValue} from './EditorModel.js'
import {formatISODate, parseFinnishDate, formatFinnishDate} from '../date.js'
import DayPicker, { DateUtils } from 'react-day-picker'

export const DateEditor = React.createClass({
  render() {
    let {model, isValid = () => true } = this.props
    let {invalidDate, valueBus} = this.state

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
      let valid = (model.optional && !event.target.value) || (date && isValid(date))
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
        if (date) {
          valueBus.push([model.context, modelSetValue(model, { data : formatISODate(date) , title: formatFinnishDate(date) })])
        } else {
          resetOptionalModel(model)
        }
      }
      model.context.errorBus.push([model.context, {error: !valid}])
      this.setState({invalidDate: !valid})
    }

    return model.context.edit
      ? (
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
                  disabledDays={day => !isValid(day)}
                />
              </div>
            </div>
          }
        </div>
      )
      : <span className="inline date">{modelTitle(model)}</span>
  },
  removeListeners() {
    document.removeEventListener('click', this.handleClickOutside, false)
  },
  getInitialState() {
    return {valueBus: Bacon.Bus(), value: modelTitle(this.props.model)}
  },
  handleClickOutside(e) {
    if(!(this.calendarInput && this.calendarInput.contains(e.target))) {
      this.removeListeners()
      this.setState({calendarOpen: false})
    }
  },
  componentWillUnmount() {
    this.removeListeners()
  },
  componentDidMount() {
    this.state.valueBus.onValue((v) => {
      this.props.model.context.changeBus.push(v)
    })
  }
})
DateEditor.canShowInline = () => true
DateEditor.handlesOptional = true

const months = ['Tammikuu', 'Helmikuu', 'Maaliskuu', 'Huhtikuu', 'Toukokuu',
  'Kesäkuu', 'Heinäkuu', 'Elokuu', 'Syyskuu', 'Lokakuu', 'Marraskuu',
  'Joulukuu']

const weekdaysShort = ['Su', 'Ma', 'Ti', 'Ke', 'To', 'Pe', 'La']

