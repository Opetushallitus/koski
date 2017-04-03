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
                      <input disabled={disabledValue === alternative.value} type="radio" name="alternative" value={ alternative.value } onChange={() => this.onChange(alternative)}/>
                      {alternative.title}
                    </label>
                  </li>
                )
              }
            </ul>
          )
        : (
             <span className={'dropdown-wrapper ' + className}><DropDown
               options={filter(query)}
               keyValue={option => option.value}
               displayValue={option => option.title}
               onSelectionChanged={option => this.onChange(option)}
               selected={defaultValue}
               onFilter={q => this.setState({query: q})}
             /></span>
          )
      : <span className="inline enum">{modelTitle(model)}</span>
  },

  componentWillMount() {
    let interestingProps = (model) => [model.alternativesPath, model.context.edit]

    this.propsE.map(props => props.model).skipDuplicates(R.eqBy(interestingProps)).onValue(model => {
      let alternativesPath = model.alternativesPath
      let edit = model.context.edit
      if (edit && alternativesPath) {
        let alternativesP = EnumEditor.AlternativesCache[alternativesPath]
        if (!alternativesP) {
          alternativesP = Http.cachedGet(alternativesPath).doError(showInternalError)
          EnumEditor.AlternativesCache[alternativesPath] = alternativesP
        }
        alternativesP.takeUntil(this.unmountE).onValue(alternatives => {
          this.setState({alternatives})
          if (model.value) {
            let foundValue = alternatives.find(a => a.value == model.value.value)
            if (!foundValue && alternatives[0]) {
              // selected value not found in options -> pick first available option or zero value if optional
              this.onChange(model.optional ? EnumEditor.zeroValue() : alternatives[0])
            }
          }
        })
      }
    })
  },

  onChange(option) {
    let model = this.props.model
    let data = model.zeroValue && option.value === model.zeroValue.value
      ? R.dissoc('value', model)
      : R.merge(model, { value: option })
    model.context.changeBus.push([model.context, data])
  },

  getInitialState() {
    return { alternatives: [] }
  }
})
EnumEditor.canShowInline = () => true
EnumEditor.zeroValue = () => ({title: 'Ei valintaa', value: 'eivalintaa'})
EnumEditor.AlternativesCache = {}
EnumEditor.handlesOptional = true
