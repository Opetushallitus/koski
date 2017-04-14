import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import {parseBool} from './util'

const isObs = x => x instanceof Bacon.Observable

export default ({options, keyValue = o => o.key, displayValue = o => o.value, selected, onSelectionChanged, selectionText = 'valitse', enableFilter = false, newItem}) => {
  let optionsP = isObs(options) ? options : Bacon.constant(options)

  enableFilter = parseBool(enableFilter)
  let selectionIndexAtom = Atom(0)
  let queryAtom = Atom(undefined)
  let openAtom = Atom(false)
  let filteredOptionsP = Bacon.combineWith(optionsP, queryAtom, Bacon.constant(displayValue), queryFilter)
  let allOptionsP = filteredOptionsP.map(opts => opts.concat(newItem ? [newItem] : []))
  var inputElem = null

  let handleOnBlur = () => openAtom.set(false)

  let onKeyDown = (allOptions) => (e) => {
    let keyHandlers = {
      ArrowUp: () => {
        selectionIndexAtom.modify(i => i === 0 ? 0 : i - 1)
      },
      ArrowDown: () => {
        e.preventDefault()
        e.stopPropagation()
        if (openAtom.get()) {
          selectionIndexAtom.modify(i => (i === allOptions.length - 1) ? i : i + 1)
        } else {
          openAtom.set(true)
        }
      },
      Escape: () => {
        openAtom.set(false)
      },
      Enter: () => {
        e.preventDefault()
        if (openAtom.get()) {
          selectOption(e, allOptions[selectionIndexAtom.get()])
        }
      }
    }
    let handler = keyHandlers[e.key]
    if (handler) handler()
  }
  let toggleOpen = () => {
    openAtom.modify(wasOpen => {
      if (!wasOpen && inputElem) inputElem.select() // nasty side effect
      return !wasOpen
    })
  }
  let handleInput = (e) => {
    queryAtom.set(e.target.value)
    openAtom.set(true)
  }
  let handleInputBlur = (allOptions) => (e) => {
    let matchingOption = allOptions.find(o => inputElem.value && displayValue(o).toLowerCase() == inputElem.value.toLowerCase())
    if (matchingOption && !R.equals(matchingOption,selected)) {
      selectOption(e, matchingOption)
    } else {
      openAtom.set(false)
      selectionIndexAtom.set(0)
      queryAtom.set(undefined)
    }
  }
  let handleMouseOver = (allOptions, o) => {
    let index = allOptions.findIndex(option => keyValue(option) == keyValue(o))
    selectionIndexAtom.set(index)
  }
  let isNewItem = (o, i) => i == options.length
  let selectOption = (e, option) => {
    e.preventDefault()
    e.stopPropagation()
    onSelectionChanged(option)
  }
  return (<span>{
    allOptionsP.map(allOptions => (<div className="dropdown" tabIndex={enableFilter ? '' : '0'} onBlur={handleOnBlur} onKeyDown={onKeyDown(allOptions)}>
          {
            enableFilter ?
              <div className="input-container" onClick={toggleOpen}>
                <input
                  type="text"
                  ref={(input => inputElem = input)}
                  onChange={handleInput}
                  onBlur={handleInputBlur(allOptions)}
                  value={queryAtom.map(query => query != undefined ? query : selected ? displayValue(selected) : selectionText)}
                  className={selected ? 'select' : 'select no-selection'}
                />
              </div> :
              <div className={selected ? 'select' : 'select no-selection'}
                   onClick={toggleOpen}>{selected ? displayValue(selected) : selectionText}
              </div>
          }
          {
            (allOptions.length > 0) && <ul className={openAtom.map(open => open ? 'options open' : 'options')}>
              {
                allOptions.map((o,i) => {
                  let isNew = isNewItem(o, i)
                  return (<li key={keyValue(o) || displayValue(o)}
                              className={selectionIndexAtom.map(selectionIndex => 'option' + (i == selectionIndex ? ' selected' : '') + (isNew ? ' new-item' : ''))}
                              onMouseDown={(e) => {selectOption(e, o)}}
                              onMouseOver={() => handleMouseOver(allOptions, o)}>
                    { isNew ? <span><span className="plus"></span>{displayValue(newItem)}</span> : displayValue(o)}
                  </li>)
                })
              }
            </ul>
          }
        </div>
      )
    )
  }</span>)
}

let queryFilter = (options, query, displayValue) => {
  if (!query) return options
  query = query.toLowerCase()
  return options.filter(o => displayValue(o).toLowerCase().startsWith(query))
}