import React from 'baret'
import Bacon from 'baconjs'
import Dropdown from '../components/Dropdown'
import {elementWithLoadingIndicator} from '../components/AjaxLoadingIndicator'
import {t} from '../i18n/i18n'
import {parseBool, toObservable} from '../util/util'
import Text from '../i18n/Text'
import { PropertyInfo } from '../editor/PropertyInfo'

/*
  className
  title (TODO: should be a localization key)
  options: Property<Koodiarvo>
  selected: Atom representing current selection
  enableFilter: true/false
  selectionText: shown when no selection
  showKoodiarvo: true/false
 */
export default ({ className, title, options, selected, enableFilter, selectionText, showKoodiarvo = false, property = undefined}) => {
  showKoodiarvo = parseBool(showKoodiarvo)
  options = toObservable(options)
  let onChange = (value) => { selected.set(value) }

  return (<label tabIndex={'-1'} className={className}><Text name={title}/>{property && <PropertyInfo property={property}/>}{
    elementWithLoadingIndicator(Bacon.combineWith(options, selected, (opts, sel) =>
    {
      if (sel && opts.length == 1) return <input type="text" className={className} disabled value={t(sel.nimi) || ''}></input>
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
