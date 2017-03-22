import React from 'react'
import R from 'ramda'
import {modelTitle} from './EditorModel.js'
import {showInternalError} from '../location.js'
import BaconComponent from '../BaconComponent'
import Http from '../http'
import DropDown from '../Dropdown.jsx'

export const EnumEditor = BaconComponent({
  render() {
    let {model, asRadiogroup, disabledValue} = this.props
    let alternatives = model.alternatives || (this.state.alternatives) || []
    let className = alternatives.length ? '' : 'loading'
    alternatives = model.context.zeroValue ? R.prepend(model.context.zeroValue, alternatives) : alternatives
    let defaultValue = (model.value && model.value.value) || model.context.zeroValue

    let onChange = (option) => {
      let data = model.context.zeroValue && option.value === model.context.zeroValue.value ? undefined : R.merge(model, { value: option })
      model.context.changeBus.push([model.context, data])
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
               options={alternatives}
               keyValue={option => option.value}
               displayValue={option => option.title}
               onSelectionChanged={option => onChange(option)}
               selected={defaultValue}
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
