import React from 'react'
import R from 'ramda'
import {modelTitle} from './EditorModel.js'
import {optionalModel} from './OptionalEditor.jsx'
import {showInternalError} from '../location.js'
import BaconComponent from '../BaconComponent'
import Http from '../http'
import DropDown from '../Dropdown.jsx'

export const EnumEditor = BaconComponent({
  render() {
    let {model, asRadiogroup, disabledValue} = this.props
    let {query} = this.state
    let alternatives = model.alternatives || (this.state.alternatives) || []
    let className = alternatives.length ? '' : 'loading'

    if (model.optional) {
      let prototype = model.value ? model
                                  : R.dissoc('value', R.merge(model, optionalModel(model))) // Replace the enum default with the zero value
      model.context.changeBus.push([prototype.context, R.merge(prototype, {optional: false, zeroValue: EnumEditor.zeroValue()})])
    }

    alternatives = model.zeroValue ? R.prepend(model.zeroValue, alternatives) : alternatives
    let defaultValue = model.value || model.zeroValue

    let onChange = (option) => {
      let data = model.zeroValue && option.value === model.zeroValue.value ? R.dissoc('value', model) : R.merge(model, { value: option })
      model.context.changeBus.push([model.context, data])
    }

    let filter = q => {
      return q ? alternatives.filter(a => a.title.toLowerCase().startsWith(q.toLowerCase())) : alternatives
    }

    return model.context.edit
      ? asRadiogroup
        ? (
            <ul className={className}>
              {
                alternatives.map(alternative =>
                  <li key={ alternative.value }>
                    <label className={disabledValue === alternative.value ? 'alternative disabled' : 'alternative'}>
                      <input disabled={disabledValue === alternative.value} type="radio" name="alternative" value={ alternative.value } onChange={() => onChange(alternative)}/>
                      {alternative.title}
                    </label>
                  </li>
                )
              }
            </ul>
          )
        : (
             <DropDown
               options={filter(query)}
               keyValue={option => option.value}
               displayValue={option => option.title}
               onSelectionChanged={option => onChange(option)}
               selected={defaultValue}
               onFilter={q => this.setState({query: q})}
             />
          )
      : <span className="inline enum">{modelTitle(model)}</span>
  },

  componentWillMount() {
    this.propsE.map((props) => [props.model.alternativesPath, props.model.context.edit]).skipDuplicates(R.equals).onValues((alternativesPath, edit) => {
      if (edit && alternativesPath) {
        let alternativesP = EnumEditor.AlternativesCache[alternativesPath]
        if (!alternativesP) {
          alternativesP = Http.cachedGet(alternativesPath).doError(showInternalError)
          EnumEditor.AlternativesCache[alternativesPath] = alternativesP
        }
        alternativesP.takeUntil(this.unmountE).onValue(alternatives => this.setState({alternatives}))
      }
    })
  },

  getInitialState() {
    return { alternatives: [] }
  }
})
EnumEditor.canShowInline = () => true
EnumEditor.zeroValue = () => ({title: 'Ei valintaa', value: 'eivalintaa'})
EnumEditor.AlternativesCache = {}
EnumEditor.handlesOptional = true
