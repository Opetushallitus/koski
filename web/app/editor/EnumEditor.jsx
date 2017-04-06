import React from 'baret'
import R from 'ramda'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {modelTitle} from './EditorModel.js'
import {wrapOptional} from './OptionalEditor.jsx'
import {showInternalError} from '../location.js'
import Http from '../http'
import DropDown from '../Dropdown.jsx'
import {modelSetValue} from './EditorModel'

export const EnumEditor = ({model, asRadiogroup, disabledValue}) => {
  let wrappedModel = wrapOptional({
    model,
    createEmpty: (protomodel) => modelSetValue(protomodel, zeroValue)
  })

  let query = Atom()
  let alternativesP = EnumEditor.fetchAlternatives(wrappedModel)
  let classNameP = alternativesP.map(xs => xs.length ? '' : 'loading')

  let alternativesWithZeroValueP = alternativesP.map(xs => wrappedModel.optional ? R.prepend(zeroValue, xs) : xs)

  let defaultValue = wrappedModel.value || zeroValue

  let filteredAlternativesP = Bacon.combineWith(alternativesWithZeroValueP, query, (xs, q) => {
    return q ? xs.filter(a => a.title.toLowerCase().startsWith(q.toLowerCase())) : xs
  })

  let onChange = (option) => {
    wrappedModel.context.changeBus.push([wrappedModel.context, modelSetValue(wrappedModel, option)])
  }

  return wrappedModel.context.edit
    ? asRadiogroup
      ? (
          <ul className={classNameP}>
            {
              alternativesP.map(alternatives =>
                alternatives.map(alternative =>
                  <li key={ alternative.value }>
                    <label className={disabledValue === alternative.value ? 'alternative disabled' : 'alternative'}>
                      <input disabled={disabledValue === alternative.value} type="radio" name="alternative" value={ alternative.value } onChange={() => onChange(alternative)}/>
                      {alternative.title}
                    </label>
                  </li>
                )
              )
            }
          </ul>
        )
      : (
           <span className={classNameP.map(n => 'dropdown-wrapper ' + n)}>
             <DropDown baret-lift
               options={filteredAlternativesP}
               keyValue={option => option.value}
               displayValue={option => option.title}
               onSelectionChanged={option => onChange(option)}
               selected={defaultValue}
               onFilter={q => query.set(q)}
             />
           </span>
        )
    : <span className="inline enum">{modelTitle(wrappedModel)}</span>
}

let zeroValue = {title: 'Ei valintaa', value: 'eivalintaa'}

EnumEditor.fetchAlternatives = (model) => {
  let alternativesPath = model.alternativesPath
  let edit = model.context.edit
  if (edit && alternativesPath) {
    let alternativesP = alternativesCache[alternativesPath]
    if (!alternativesP) {
      alternativesP = Http.cachedGet(alternativesPath).doError(showInternalError).startWith([])
      alternativesCache[alternativesPath] = alternativesP
    }
    return alternativesP
  } else {
    return Bacon.constant([])
  }
}

let alternativesCache = {}

EnumEditor.canShowInline = () => true
EnumEditor.handlesOptional = true
