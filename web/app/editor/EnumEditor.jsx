import React from 'react'
import R from 'ramda'
import {modelTitle} from './EditorModel.js'
import {showInternalError} from '../location.js'
import BaconComponent from '../BaconComponent'
import Http from '../http'

export const EnumEditor = BaconComponent({
  render() {
    let {model, asRadiogroup} = this.props
    let alternatives = model.alternatives || (this.state.alternatives) || []
    let className = alternatives.length ? '' : 'loading'

    let onChange = (event) => {
      let selected = alternatives.find(alternative => alternative.value == event.target.value)
      model.context.changeBus.push([model.context, selected])
    }

    return model.context.edit
      ? asRadiogroup
        ? (
            <ul className={className}>
              {
                alternatives.map(alternative =>
                  <li key={ alternative.value }>
                    <label>
                      <input type="radio" name="alternative" value={ alternative.value } onChange={onChange}></input>
                      {alternative.title}
                    </label>
                  </li>
                )
              }
            </ul>
          )
        : (
            <select className={className} defaultValue={model.value && model.value.value} onChange={ onChange }>
              {
                alternatives.map(alternative =>
                  <option value={ alternative.value } key={ alternative.value }>{alternative.title}</option>
                )
              }
            </select>
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
EnumEditor.AlternativesCache = {}
