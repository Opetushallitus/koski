import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import {parseBool, toObservable} from './util'
import {elementWithLoadingIndicator} from './AjaxLoadingIndicator.jsx'
import {t} from './i18n'

/*
  options: [] or Observable []
  keyValue: item => string
  displayValue: item => string
  selected: currently selected item or Observable
  onSelectionChanged: callback
  newItem: proto for new item
  isRemovable: item => boolean
  onRemoval: callback
  removeText: string
  enableFilter: boolean
  selectionText: shown when no option is selected
 */
export default ({ options, keyValue = o => o.key, displayValue = o => o.value,
                  selected, onSelectionChanged, selectionText = t('Valitse...'),
                  enableFilter = false,
                  newItem, isRemovable = () => false, onRemoval, removeText}) => {
  let optionsP = toObservable(options)
  let selectedP = toObservable(selected)

  enableFilter = parseBool(enableFilter)
  let selectionIndexAtom = Atom(0)
  let removeIndexAtom = Atom(undefined)
  let queryAtom = Atom(undefined)
  let openAtom = Atom(false)
  selectedP.changes().onValue(() => openAtom.set(false))
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
  let selectRemoval = (e, option) => {
    e.preventDefault()
    e.stopPropagation()
    onRemoval(keyValue(option))
  }
  return (<span>{
    elementWithLoadingIndicator(allOptionsP.map(allOptions => (<div className="dropdown" tabIndex={enableFilter ? '' : '0'} onBlur={handleOnBlur} onKeyDown={onKeyDown(allOptions)}>
          {
            enableFilter ?
              <div className="input-container" onClick={toggleOpen}>
                <input
                  type="text"
                  ref={(input => inputElem = input)}
                  onChange={handleInput}
                  onBlur={selectedP.map(s => handleInputBlur(allOptions, s))}
                  value={Bacon.combineWith(queryAtom, selectedP, (q, s) => {
                    return q != undefined ? q : s ? displayValue(s) : ''
                  })}
                  placeholder={selectionText}
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
                  let className = Bacon.combineWith(
                    (s, r) => s + r,
                    selectionIndexAtom.map(selectionIndex => 'option' + (i === selectionIndex ? ' selected' : '') + (isNew ? ' new-item' : '')),
                    removeIndexAtom.map(removeIndex => removeIndex === i ? ' removing' : ''))
                  return (<li key={keyValue(o) || displayValue(o)}
                              className={className}
                              onMouseDown={(e) => {selectOption(e, o)}}
                              onClick={(e) => {selectOption(e, o)}}
                              onMouseOver={() => handleMouseOver(allOptions, o)}>
                    {
                      isNew ?
                        <span><span className="plus">{''}</span>{displayValue(newItem)}</span> :
                        isRemovable(o) ?
                          <span className="removable-option" title={removeText}>{displayValue(o)}
                            <a className="remove-value"
                               onMouseDown={(e) => {selectRemoval(e, o)}}
                               onClick={(e) => {selectRemoval(e, o)}}
                               onMouseOver={() => removeIndexAtom.set(i)}
                               onMouseLeave={() => removeIndexAtom.set(undefined)}
                            >{''}</a>
                          </span> :
                          displayValue(o)
                    }
                  </li>)
                })
              }
            </ul>
          }
        </div>
      )
    ))
  }</span>)
}

let queryFilter = (options, query, displayValue) => {
  if (!query) return options
  query = query.toLowerCase()
  return options.filter(o => displayValue(o).toLowerCase().includes(query))
}