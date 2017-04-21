import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import {parseBool} from './util'

const isObs = x => x instanceof Bacon.Observable

export default ({options, keyValue = o => o.key, displayValue = o => o.value, selected, onSelectionChanged, selectionText = 'valitse', enableFilter = false, newItem}) => {
  let optionsP = isObs(options) ? options : Bacon.constant(options)

  let selectedP = isObs(selected) ? selected : Bacon.constant(selected)

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
          var selectionIndex = selectionIndexAtom.get()
          var selectedOption = allOptions[selectionIndex]
          selectedOption && selectOption(e, selectedOption)
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
  let handleInputBlur = (allOptions, s) => (e) => {
    let matchingOption = allOptions.find(o => inputElem.value && displayValue(o).toLowerCase() == inputElem.value.toLowerCase())
    if (matchingOption && !R.equals(matchingOption,s)) {
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
  let isNewItem = (allOptions, o, i) => newItem && i == allOptions.length - 1
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
                  onBlur={selectedP.map(s => handleInputBlur(allOptions, s))}
                  value={Bacon.combineWith(queryAtom, selectedP, (q, s) => {
                    return q != undefined ? q : s ? displayValue(s) : selectionText
                  })}
                  className={selectedP.map(s => s ? 'select' : 'select no-selection')}
                />
              </div> :
              <div className={selectedP.map(s => s ? 'select' : 'select no-selection')}
                   onClick={toggleOpen}>{selectedP.map(s => s ? displayValue(s) : selectionText)}
              </div>
          }
          {
            (allOptions.length > 0) && <ul className={openAtom.map(open => open ? 'options open' : 'options')}>
              {
                allOptions.map((o,i) => {
                  let isNew = isNewItem(allOptions, o, i)
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