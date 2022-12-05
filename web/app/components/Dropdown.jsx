import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as R from 'ramda'
import {
  flatMapArray,
  parseBool,
  scrollElementBottomVisible,
  toObservable
} from '../util/util'
import { elementWithLoadingIndicator } from './AjaxLoadingIndicator'
import { t } from '../i18n/i18n'
import { buildClassNames } from './classnames'

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
  inline: hide borders until hovered upon
 */
export default ({
  options,
  keyValue = (o) => o.key,
  displayValue = (o) => o.value,
  selected,
  onSelectionChanged,
  selectionText = t('Valitse...'),
  inline = false,
  enableFilter = false,
  newItem,
  isRemovable = () => false,
  onRemoval,
  removeText,
  isOptionEnabled = () => true,
  itemTestId = (o) => o.testId,
  ...rest
}) => {
  options = toObservable(options)
  const selectedP = toObservable(selected)
  inline = parseBool(inline)
  enableFilter = parseBool(enableFilter)
  const selectionIndexAtom = Atom(0)
  const removeIndexAtom = Atom(undefined)
  const queryAtom = Atom(undefined)
  const openAtom = Atom(false)
  selectedP.changes().onValue(() => openAtom.set(false))
  const filteredOptionsP = Bacon.combineWith(
    options,
    queryAtom,
    Bacon.constant(displayValue),
    queryFilter
  )
  const allOptionsP = filteredOptionsP.map((opts) =>
    opts.concat(newItem ? [newItem] : [])
  )
  let inputElem = null
  let listElem = null
  const handleOnBlur = () => openAtom.set(false)
  openAtom
    .filter(R.identity)
    .delay(0)
    .onValue(() => scrollElementBottomVisible(listElem))

  const onKeyDown = (allOptions) => (e) => {
    const keyHandlers = {
      ArrowUp: () => {
        selectionIndexAtom.modify((i) => (i === 0 ? 0 : i - 1))
      },
      ArrowDown: () => {
        e.preventDefault()
        e.stopPropagation()
        if (openAtom.get()) {
          selectionIndexAtom.modify((i) =>
            i === allOptions.length - 1 ? i : i + 1
          )
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
          const selectionIndex = selectionIndexAtom.get()
          const selectedOption = allOptions[selectionIndex]
          selectedOption && selectOption(e, selectedOption)
        }
      }
    }
    const handler = keyHandlers[e.key]
    if (handler) handler()
  }
  const toggleOpen = () => {
    openAtom.modify((wasOpen) => {
      if (!wasOpen && inputElem) inputElem.select() // nasty side effect
      return !wasOpen
    })
  }
  const handleInput = (e) => {
    queryAtom.set(e.target.value)
    openAtom.set(true)
  }
  const handleInputBlur = (allOptions, s) => (e) => {
    if (!inputElem) return
    const matchingOptions = allOptions.filter(
      (o) =>
        inputElem.value &&
        displayValue(o).toLowerCase() === inputElem.value.toLowerCase()
    )
    if (!R.isEmpty(matchingOptions) && !R.includes(s, matchingOptions)) {
      // if multiple options have the same display value (e.g. arviointiasteikkoammatillinent1k3 and ...15),
      // try to use the selected (highlighted one) when parsing explicitly typed input.
      const selectionIndex = selectionIndexAtom.get()
      const selectedOption = allOptions[selectionIndex]
      const option =
        selectedOption && R.includes(selectedOption, matchingOptions)
          ? selectedOption
          : matchingOptions[0]
      selectOption(e, option)
    } else {
      openAtom.set(false)
      selectionIndexAtom.set(0)
      queryAtom.set(undefined)
    }
  }
  const handleMouseOver = (allOptions, o) => {
    const index = allOptions.findIndex(
      (option) => keyValue(option) === keyValue(o)
    )
    selectionIndexAtom.set(index)
  }
  const isNewItem = (allOptions, o, i) => newItem && i === allOptions.length - 1
  const selectOption = (e, option) => {
    e.preventDefault()
    e.stopPropagation()
    if (isOptionEnabled(option)) {
      onSelectionChanged(option)
      openAtom.set(false)
    }
  }
  const selectRemoval = (e, option) => {
    e.preventDefault()
    e.stopPropagation()
    onRemoval(option)
  }

  return (
    <span>
      {elementWithLoadingIndicator(
        allOptionsP.map((allOptions) => {
          const grouped =
            R.keys(
              R.groupBy((opt) => opt.groupName)(
                R.filter((o) => o.groupName, allOptions)
              )
            ).length > 1
          const className = buildClassNames([
            'dropdown',
            inline && 'inline',
            grouped && 'grouped'
          ])
          return (
            <div
              className={className}
              tabIndex={enableFilter ? '' : '0'}
              onBlur={handleOnBlur}
              onKeyDown={onKeyDown(allOptions)}
              role="combobox"
              {...rest}
            >
              {enableFilter ? (
                <div className="input-container" onClick={toggleOpen}>
                  <input
                    type="text"
                    role="combobox"
                    data-testid="koodisto-dropdown-multi-selection-input"
                    aria-owns="options-list"
                    ref={(input) => (inputElem = input)}
                    onChange={handleInput}
                    onBlur={selectedP.map((s) =>
                      handleInputBlur(allOptions, s)
                    )}
                    value={Bacon.combineWith(queryAtom, selectedP, (q, s) => {
                      return q !== undefined ? q : s ? displayValue(s) : ''
                    })}
                    aria-label={Bacon.combineWith(
                      queryAtom,
                      selectedP,
                      (q, s) => {
                        return q !== undefined
                          ? q
                          : s
                          ? displayValue(s)
                          : selectionText
                      }
                    )}
                    placeholder={selectionText}
                    className={selectedP.map((s) =>
                      s ? 'select' : 'select no-selection'
                    )}
                  />
                </div>
              ) : (
                <div
                  className={selectedP.map((s) =>
                    s ? 'select' : 'select no-selection'
                  )}
                  role="combobox"
                  aria-label={selectedP.map((s) =>
                    s ? displayValue(s) : selectionText
                  )}
                  aria-owns="options-list"
                  onClick={toggleOpen}
                >
                  {selectedP.map((s) => (s ? displayValue(s) : selectionText))}
                </div>
              )}
              {allOptions.length > 0 && (
                <ul
                  className={openAtom.map((open) =>
                    open ? 'options open' : 'options'
                  )}
                  aria-expanded={open}
                  ref={(ref) => (listElem = ref)}
                  id="options-list"
                  data-testid="koodisto-dropdown-all-options"
                  role="listbox"
                >
                  {flatMapArray(allOptions, (o, i) => {
                    const isNew = isNewItem(allOptions, o, i)
                    const isZeroValue = keyValue(o) === 'eivalintaa'
                    const itemClassName = Bacon.combineWith(
                      (s, r, a) => s + r + a,
                      selectionIndexAtom.map((selectionIndex) =>
                        buildClassNames([
                          'option',
                          i === selectionIndex &&
                            isOptionEnabled(o) &&
                            'selected',
                          isNew && 'new-item',
                          isZeroValue && 'zero-value'
                        ])
                      ),
                      removeIndexAtom.map((removeIndex) =>
                        removeIndex === i ? ' removing' : ''
                      ),
                      isOptionEnabled(o) ? '' : ' option-disabled'
                    )
                    const itemElement = (
                      <li
                        role="listitem"
                        key={keyValue(o) || displayValue(o)}
                        aria-label={displayValue(o)}
                        className={itemClassName}
                        onMouseDown={(e) => {
                          selectOption(e, o)
                        }}
                        onClick={(e) => {
                          selectOption(e, o)
                        }}
                        onMouseOver={() => handleMouseOver(allOptions, o)}
                        data-testid={itemTestId(o)}
                      >
                        {isNew ? (
                          <span>
                            <span className="plus">{''}</span>
                            {displayValue(newItem)}
                          </span>
                        ) : isRemovable(o) ? (
                          <span className="removable-option" title={removeText}>
                            {displayValue(o)}
                            <a
                              className="remove-value"
                              onMouseDown={(e) => {
                                selectRemoval(e, o)
                              }}
                              onClick={(e) => {
                                selectRemoval(e, o)
                              }}
                              onMouseOver={() => removeIndexAtom.set(i)}
                              onMouseLeave={() =>
                                removeIndexAtom.set(undefined)
                              }
                            />
                          </span>
                        ) : (
                          displayValue(o)
                        )}
                      </li>
                    )
                    const groupName =
                      grouped &&
                      (i === 0 || allOptions[i - 1].groupName !== o.groupName)
                        ? o.groupName
                        : ''
                    if (groupName) {
                      return [
                        <li key={groupName} className="group-header">
                          {groupName}
                        </li>,
                        itemElement
                      ]
                    } else {
                      return [itemElement]
                    }
                  })}
                </ul>
              )}
            </div>
          )
        })
      )}
    </span>
  )
}

const queryFilter = (options, query, displayValue) => {
  if (!query) return options
  query = query.toLowerCase()
  return options.filter((o) => displayValue(o).toLowerCase().includes(query))
}
