import {parseFinnishDate, formatFinnishDate} from './date.js'
import React from 'react'

export default React.createClass({
  render() {
    const {from, to, invalidStartDate, invalidEndDate} = this.state
    console.log(this.state)
    return (
      <div className="calendar" onMouseDown={ this.handleContainerMouseDown } tabIndex="0" onBlur={this.handleInputBlur}
           onFocus={this.handleInputFocus}>
        <div
          className="calendar-selection">{ from && to ? (formatFinnishDate(from) + '-' + formatFinnishDate(to)) : 'kaikki'}</div>
        { this.state.open &&
        <div className="DayPicker-CalendarContainer">
          <div className="date-range">
            <label>Aloituspäivä</label>
            <input
              className={invalidStartDate ? 'error' : ''}
              type="text"
              value={invalidStartDate ? invalidStartDate.value : from ? formatFinnishDate(from) : ''}
              onChange={this.handleStartDate}
            />&mdash;
            <input
              className={invalidEndDate ? 'error' : ''}
              type="text"
              value={invalidEndDate ? invalidEndDate.value : to ? formatFinnishDate(to) : ''}
              onChange={this.handleEndDate}
            />
          </div>
          <div className="calendar-shortcuts">
            <button
              className="button"
              onClick={() => this.handleRangeSelection({from: undefined, to: undefined})}>kaikki
            </button>
            <button
              className="button"
              onClick={() => this.handleRangeSelection({from: new Date(new Date().getFullYear(), 0, 1), to: new Date()})}>kuluva vuosi
            </button>
            <button
              className="button"
              onClick={() => this.handleRangeSelection({from: new Date(new Date().getFullYear() - 1, 0, 1), to: new Date(new Date().getFullYear() - 1, 11, 31)})}>edellinen vuosi
            </button>
          </div>
        </div>
        }
      </div>
    )
  },
  handleStartDate(e) {
    const day = parseFinnishDate(e.target.value)
    const isValidStartDate = !e.target.value || (day && isPastOrOrToday(day) && isBeforeOrSame(day, this.state.to))
    const from = (!e.target.value || !isValidStartDate) ? undefined : day
    const invalidStartDate = isValidStartDate ? undefined : {value: e.target.value}
    const newState = Object.assign(
      {
        from: from,
        invalidStartDate: invalidStartDate
      },
      this.state.invalidEndDate ? calculateEndState(this.state.invalidEndDate.value, from) : {to: this.state.to}
    )
    this.setState(newState, () => this.props.onSelectionChanged({from: newState.from, to: newState.to}))
  },
  handleEndDate(e) {
    const day = parseFinnishDate(e.target.value)
    const isValidEndDate = !e.target.value || (day && isPastOrOrToday(day) && isAfterOrSame(day, this.state.from))
    const to = (!e.target.value || !isValidEndDate) ? undefined : day
    const invalidEndDate = isValidEndDate ? undefined : {value: e.target.value}
    const newState = Object.assign(
      {
        to: to,
        invalidEndDate: invalidEndDate
      },
      this.state.invalidStartDate ? calculateStartState(this.state.invalidStartDate.value, to) : {from: this.state.from}
    )
    this.setState(newState, () => this.props.onSelectionChanged({from: newState.from, to: newState.to}))
  },
  handleRangeSelection(range) {
    this.setState(range, () => this.props.onSelectionChanged(range))
  },
  getInitialState() {
    return {
      from: this.props.selectedStartDay && parseFinnishDate(this.props.selectedStartDay),
      to: this.props.selectedEndDay && parseFinnishDate(this.props.selectedEndDay)
    }
  },
  componentDidMount() {
    window.addEventListener('click', this.handleClickOutside, false)
  },
  componentWillUnmount() {
    window.removeEventListener('click', this.handleClickOutside, false)
  },
  handleClickOutside(e) {
    //!e.target.closest('.calendar') && this.setState({open: false})
  },
  handleContainerMouseDown() {
    this.clickedInside = true
    this.clickTimeout = setTimeout(() => {
      this.clickedInside = false
    }, 0)
  },
  handleInputFocus() {
    this.setState({open: true})
  },
  handleInputBlur() {
    //this.setState({open: this.clickedInside})
  }
})

const calculateEndState = (endValue, fromDate) => {
  const endDate = endValue ? parseFinnishDate(endValue) : undefined
  const isValidEndDate = !endValue || (endDate && isPastOrOrToday(endDate) && isAfterOrSame(endDate, fromDate))
  return {
    to: !endValue ? undefined : endDate,
    invalidEndDate: isValidEndDate ? undefined : {value: endValue}
  }
}

const calculateStartState = (startValue, toDate) => {
  const startDate = startValue ? parseFinnishDate(startValue) : undefined
  const isValidStartDate = !startValue || (startDate && isPastOrOrToday(startDate) && isBeforeOrSame(startDate, toDate))
  return {
    from: !startValue ? undefined : startDate,
    invalidStartDate: isValidStartDate ? undefined : {value: startValue}
  }
}

const isPastOrOrToday = d => isSameDay(d, new Date()) || isPastDay(d)

const isBeforeOrSame = (d1, d2) => {
  if(!d2) {
    return true
  }
  return d1 <= d2
}

const isAfterOrSame = (d1, d2) => {
  if(!d2) {
    return true
  }
  return d1 >= d2
}

const isSameDay = (d1, d2) => {
  if (!d1 || !d2) {
    return false
  }
  return d1.getDate() === d2.getDate() && d1.getMonth() === d2.getMonth() && d1.getFullYear() === d2.getFullYear()
}

const isPastDay = d => {
  var today = new Date()
  today.setHours(0, 0, 0, 0)
  return d < today
}