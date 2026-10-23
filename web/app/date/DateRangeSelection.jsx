import { formatFinnishDate, parseFinnishDate } from './date'
import React, { useLayoutEffect, useRef, useState } from 'react'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'

export default function DateRangeSelection(props) {
  const [range, setRange] = useState(() => ({
    from: props.selectedStartDay && parseFinnishDate(props.selectedStartDay),
    to: props.selectedEndDay && parseFinnishDate(props.selectedEndDay)
  }))
  const { from, to, invalidStartDate, invalidEndDate } = range
  const [open, setOpen] = useState()
  const rootRef = useRef(null)
  const startDateInputRef = useRef(null)

  useLayoutEffect(() => {
    const handleClickOutside = (e) => {
      !e.target.closest('.date-range') && setOpen(false)
    }
    const handleFocus = (e) => {
      const focusInside =
        e.target === window ? false : !!rootRef.current.contains(e.target)
      setOpen((wasOpen) => wasOpen && focusInside)
    }

    window.addEventListener('click', handleClickOutside, false)
    window.addEventListener('focus', handleFocus, true)
    return () => {
      window.removeEventListener('click', handleClickOutside, false)
      window.removeEventListener('focus', handleFocus, true)
    }
  }, [])

  useLayoutEffect(() => {
    if (open) {
      startDateInputRef.current.focus()
    }
  }, [open])

  const handleStartDate = (e) => {
    const newStartDate = calculateStartState(e.target.value, to)
    const newState = Object.assign(
      newStartDate,
      invalidEndDate
        ? calculateEndState(invalidEndDate.value, newStartDate.from)
        : { to }
    )
    setRange((previous) => ({ ...previous, ...newState }))
    props.onSelectionChanged({ from: newState.from, to: newState.to })
  }

  const handleEndDate = (e) => {
    const newEndDate = calculateEndState(e.target.value, from)
    const newState = Object.assign(
      newEndDate,
      invalidStartDate
        ? calculateStartState(invalidStartDate.value, newEndDate.to)
        : { from }
    )
    setRange((previous) => ({ ...previous, ...newState }))
    props.onSelectionChanged({ from: newState.from, to: newState.to })
  }

  const handleRangeSelection = (selection) => {
    setRange((previous) => ({ ...previous, ...selection }))
    setOpen(false)
    props.onSelectionChanged(selection)
  }

  const toggleOpen = () => setOpen((wasOpen) => !wasOpen)

  const onKeyDown = (e) => {
    switch (e.key) {
      case 'Enter':
        e.preventDefault()
        setOpen(false)
        break
      case 'Escape':
        setOpen(false)
        break
      case 'ArrowDown':
        e.stopPropagation()
        setOpen(true)
        break
    }
  }

  return (
    <div
      className="date-range"
      onKeyDown={onKeyDown}
      tabIndex="0"
      ref={rootRef}
    >
      <div onClick={toggleOpen} className="date-range-selection">
        {from || to
          ? (from ? formatFinnishDate(from) : '') +
            '-' +
            (to ? formatFinnishDate(to) : '')
          : t('kaikki')}
      </div>
      {open && (
        <div className="date-range-container">
          <div className="date-range-input">
            <label>
              <Text name={props.label || 'Aloituspäivä'} />
            </label>
            <input
              className={invalidStartDate ? 'start error' : 'start'}
              type="text"
              value={
                invalidStartDate
                  ? invalidStartDate.value
                  : from
                    ? formatFinnishDate(from)
                    : ''
              }
              onChange={handleStartDate}
              ref={startDateInputRef}
            />
            {'—'}
            <input
              className={invalidEndDate ? 'end error' : 'end'}
              type="text"
              value={
                invalidEndDate
                  ? invalidEndDate.value
                  : to
                    ? formatFinnishDate(to)
                    : ''
              }
              onChange={handleEndDate}
            />
          </div>
          <div className="date-range-shortcuts">
            <button
              className="koski-button"
              onClick={() =>
                handleRangeSelection({ from: undefined, to: undefined })
              }
            >
              <Text name="kaikki" />
            </button>
            <button
              className="koski-button"
              onClick={() =>
                handleRangeSelection({
                  from: new Date(new Date().getFullYear(), 0, 1),
                  to: new Date()
                })
              }
            >
              <Text name="kuluva vuosi" />
            </button>
            <button
              className="koski-button"
              onClick={() =>
                handleRangeSelection({
                  from: new Date(new Date().getFullYear() - 1, 0, 1),
                  to: new Date(new Date().getFullYear() - 1, 11, 31)
                })
              }
            >
              <Text name="edellinen vuosi" />
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

const calculateEndState = (endValue, fromDate) => {
  const endDate = parseFinnishDate(endValue)
  const isValidEndDate =
    !endValue ||
    (endDate && isPastOrOrToday(endDate) && isAfterOrSame(endDate, fromDate))
  return {
    to: !endValue || !isValidEndDate ? undefined : endDate,
    invalidEndDate: isValidEndDate ? undefined : { value: endValue }
  }
}

const calculateStartState = (startValue, toDate) => {
  const startDate = parseFinnishDate(startValue)
  const isValidStartDate =
    !startValue ||
    (startDate &&
      isPastOrOrToday(startDate) &&
      isBeforeOrSame(startDate, toDate))
  return {
    from: !startValue || !isValidStartDate ? undefined : startDate,
    invalidStartDate: isValidStartDate ? undefined : { value: startValue }
  }
}

const isPastOrOrToday = (d) => isSameDay(d, new Date()) || isPastDay(d)

const isBeforeOrSame = (d1, d2) => {
  if (!d2) {
    return true
  }
  return d1 <= d2
}

const isAfterOrSame = (d1, d2) => {
  if (!d2) {
    return true
  }
  return d1 >= d2
}

const isSameDay = (d1, d2) => {
  if (!d1 || !d2) {
    return false
  }
  return (
    d1.getDate() === d2.getDate() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getFullYear() === d2.getFullYear()
  )
}

const isPastDay = (d) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return d < today
}
