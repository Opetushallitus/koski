import React from 'baret'
import Bacon from 'baconjs'
import Dropdown from '../Dropdown.jsx'

export default ({ className, title, optionsP, atom}) => {
  let onChange = (value) => { atom.set(value) }
  let optionCountP = Bacon.combineWith(optionsP, atom, (options, selected) => !!selected && options.length).skipDuplicates()

  return (<div>{
    optionCountP.map(count =>
    {
      if (!count) return null
      if (count == 1) return <label className={className}>{title}<input type="text" className={className} disabled value={atom.map('.nimi.fi').map(x => x || '')}></input></label>
      return (<label className={className}>{title}<Dropdown
        options={optionsP}
        keyValue={option => option.koodiarvo}
        displayValue={option => option.nimi.fi}
        onSelectionChanged={option => onChange(option)}
        selected={atom}/></label>)
    })
  }</div>)
}
