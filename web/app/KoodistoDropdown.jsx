import React from 'baret'
import Bacon from 'baconjs'
import Dropdown from './Dropdown.jsx'
import {elementWithLoadingIndicator} from './AjaxLoadingIndicator.jsx'
import {t} from './i18n'
import {parseBool, toObservable} from './util'

export default ({ className, title, options, selected, enableFilter, selectionText, showKoodiarvo = false}) => {
  showKoodiarvo = parseBool(showKoodiarvo)
  options = toObservable(options)
  let onChange = (value) => { selected.set(value) }

  return (<label className={className}>{title}{
    elementWithLoadingIndicator(Bacon.combineWith(options, selected, (opts, sel) =>
    {
      if (sel && opts.length == 1) return <input type="text" className={className} disabled value={selected.map('.nimi.fi').map(x => x || '')}></input>
      return (<Dropdown
        options={options}
        keyValue={option => option.koodiarvo}
        displayValue={option => (showKoodiarvo ? option.koodiarvo + ' ' : '') + t(option.nimi)}
        onSelectionChanged={option => onChange(option)}
        selected={selected}
        enableFilter={enableFilter}
        selectionText={selectionText}
      />)
    }))
  }</label>)
}