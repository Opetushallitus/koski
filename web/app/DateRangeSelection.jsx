import {parseFinnishDate, formatFinnishDate} from './date.js'
import React from 'react'

export default React.createClass({
  render() {
    const {from, to, invalidStartDate, invalidEndDate} = this.state
    return (
      <div className="date-range" onKeyDown={this.onKeyDown} tabIndex="0">
        <div
          onClick={this.toggleOpen}
          className="date-range-selection">{ (from || to) ? ((from ? formatFinnishDate(from) : '') + '-' + (to ? formatFinnishDate(to) : '')) : 'kaikki'}</div>
        { this.state.open &&
        <div className="date-range-container">
          <div className="date-range-input">
            <label>Aloituspäivä</label>
            <input
              className={invalidStartDate ? 'error' : ''}
              type="text"
              value={invalidStartDate ? invalidStartDate.value : from ? formatFinnishDate(from) : ''}
              onChange={this.handleStartDate}
              ref={input => this.startDateInput = input}
            />&mdash;
            <input
              className={invalidEndDate ? 'error' : ''}
              type="text"
              value={invalidEndDate ? invalidEndDate.value : to ? formatFinnishDate(to) : ''}
              onChange={this.handleEndDate}
            />
          </div>
          <div className="date-range-shortcuts">
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
    const newStartDate = calculateStartState(e.target.value, this.state.to)
    const newState = Object.assign(
      newStartDate,
      this.state.invalidEndDate ? calculateEndState(this.state.invalidEndDate.value, newStartDate.from) : {to: this.state.to}
    )
    this.setState(newState, () => this.props.onSelectionChanged({from: newState.from, to: newState.to}))
  },
  handleEndDate(e) {
    const newEndDate = calculateEndState(e.target.value, this.state.from)
    const newState = Object.assign(
      newEndDate,
      this.state.invalidStartDate ? calculateStartState(this.state.invalidStartDate.value, newEndDate.to) : {from: this.state.from}
    )
    this.setState(newState, () => this.props.onSelectionChanged({from: newState.from, to: newState.to}))
  },
  handleRangeSelection(range) {
    this.setState(range, () => this.props.onSelectionChanged(range))
    this.setState({open: false})
  },
  getInitialState() {
    return {
      from: this.props.selectedStartDay && parseFinnishDate(this.props.selectedStartDay),
      to: this.props.selectedEndDay && parseFinnishDate(this.props.selectedEndDay)
    }
  },
  componentDidUpdate(prevProps, prevState) {
    if(this.state.open && !prevState.open) {
      this.startDateInput.focus()
    }
  },
  componentDidMount() {
    window.addEventListener('click', this.handleClickOutside, false)
  },
  componentWillUnmount() {
    window.removeEventListener('click', this.handleClickOutside, false)
  },
  handleClickOutside(e) {
    !e.target.closest('.date-range') && this.setState({open: false})
  },
  toggleOpen(e) {
    this.setState({open: !this.state.open})
  },
  onKeyDown(e) {
    let handler = this.keyHandlers[e.key]
    if(handler) {
      handler.call(this, e)
    }
  },
  keyHandlers: {
    Enter(e) {
      e.preventDefault()
      this.setState({open: false})
    },
    Escape() {
      this.setState({open: false})
    }
  }
})

const calculateEndState = (endValue, fromDate) => {
  const endDate = parseFinnishDate(endValue)
  const isValidEndDate = !endValue || (endDate && isPastOrOrToday(endDate) && isAfterOrSame(endDate, fromDate))
  return {
    to: (!endValue || !isValidEndDate) ? undefined : endDate,
    invalidEndDate: isValidEndDate ? undefined : {value: endValue}
  }
}

const calculateStartState = (startValue, toDate) => {
  const startDate = parseFinnishDate(startValue)
  const isValidStartDate = !startValue || (startDate && isPastOrOrToday(startDate) && isBeforeOrSame(startDate, toDate))
  return {
    from: (!startValue || !isValidStartDate) ? undefined : startDate,
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