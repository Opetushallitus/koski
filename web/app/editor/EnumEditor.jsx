import React from 'baret'
import R from 'ramda'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {modelTitle} from './EditorModel.js'
import {optionalModel} from './OptionalEditor.jsx'
import {showInternalError} from '../location.js'
import Http from '../http'
import DropDown from '../Dropdown.jsx'
import {doActionWhileMounted} from '../util'

let fetchAlternatives = (model) => {
  let alternativesPath = model.alternativesPath
  let edit = model.context.edit
  if (edit && alternativesPath) {
    let alternativesP = EnumEditor.AlternativesCache[alternativesPath]
    if (!alternativesP) {
      alternativesP = Http.cachedGet(alternativesPath).doError(showInternalError).startWith([])
      EnumEditor.AlternativesCache[alternativesPath] = alternativesP
    }
    return alternativesP
  } else {
    return Bacon.constant([])
  }
}

export const EnumEditor = ({model, asRadiogroup, disabledValue}) => {
  let query = Atom()
  let alternativesP = fetchAlternatives(model)
  let classNameP = alternativesP.map(xs => xs.length ? '' : 'loading')

  if (model.optional) { // TODO: replace with wrapOptional construct
    let prototype = model.value ? model
                                : R.dissoc('value', R.merge(model, optionalModel(model))) // Replace the enum default with the zero value
    model.context.changeBus.push([prototype.context, R.merge(prototype, {optional: false, zeroValue: EnumEditor.zeroValue()})])
  }

  // TODO: get rid of zeroValue
  let alternativesWithZeroValueP = alternativesP.map(xs => model.zeroValue ? R.prepend(model.zeroValue, xs) : xs)

  let defaultValue = model.value || model.zeroValue

  let selectDefaultValue = (alternatives) => {
    if (model.value) {
      let foundValue = alternatives.find(a => a.value == model.value.value)
      if (!foundValue && alternatives[0]) {
        setTimeout(function() { // Not very nice
          // selected value not found in options -> pick first available option or zero value if optional
          onChange(model.optional ? EnumEditor.zeroValue() : alternatives[0])
        }, 0)
      }
    }
  }

  let filteredAlternativesP = Bacon.combineWith(alternativesWithZeroValueP, query, (xs, q) => {
    return q ? xs.filter(a => a.title.toLowerCase().startsWith(q.toLowerCase())) : xs
  })

  let onChange = (option) => {
    let data = model.zeroValue && option.value === model.zeroValue.value
      ? R.dissoc('value', model)
      : R.merge(model, { value: option })
    model.context.changeBus.push([model.context, data])
  }

  return model.context.edit
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
             { doActionWhileMounted(alternativesP, selectDefaultValue) }
           </span>
        )
    : <span className="inline enum">{modelTitle(model)}</span>
}

EnumEditor.canShowInline = () => true
EnumEditor.zeroValue = () => ({title: 'Ei valintaa', value: 'eivalintaa'})
EnumEditor.AlternativesCache = {}
EnumEditor.handlesOptional = true
