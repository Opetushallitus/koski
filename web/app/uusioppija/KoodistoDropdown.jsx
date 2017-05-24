import React from 'baret'
import Bacon from 'baconjs'
import Dropdown from '../Dropdown.jsx'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import {t} from '../i18n'

export default ({ className, title, optionsP, atom}) => {
  let onChange = (value) => { atom.set(value) }
  let optionCountP = Bacon.combineWith(optionsP, atom, (options, selected) => !!selected && options.length).skipDuplicates()

  return (<label className={className}>{title}{
    elementWithLoadingIndicator(optionCountP.map(count =>
    {
      if (count == 1) return <input type="text" className={className} disabled value={atom.map('.nimi.fi').map(x => x || '')}></input>
      return (<Dropdown
        options={optionsP}
        keyValue={option => option.koodiarvo}
        displayValue={option => t(option.nimi)}
        onSelectionChanged={option => onChange(option)}
        selected={atom}/>)
    }))
  }</label>)
}