import React from 'react'
import Bacon from 'baconjs'
import {modelData, modelSetData} from './EditorModel.js'
import {resetOptionalModel} from './OptionalEditor.jsx'

export const StringEditor = React.createClass({
  render() {
    let {model} = this.props
    let {valueBus, error} = this.state

    let onChange = (event) => {
      let value = event.target.value
      let updatedModel = modelSetData(model, value)
      this.validate(updatedModel)

      if (!value && model.optional) {
        resetOptionalModel(model)
      } else {
        valueBus.push([model.context, updatedModel])
      }
    }

    let data = modelData(model)
    return model.context.edit
      ? <input className={error ? 'error' : 'valid'} type="text" defaultValue={data} onChange={ onChange }></input>
      : <span className="inline string">{!data ? '' : data.split('\n').map((line, k) => <span key={k}>{line}<br/></span>)}</span>
  },

  validate(model) {
    let err = {error: !model.optional && !modelData(model)}
    this.setState(err)
    model.context.errorBus.push([model.context, err])
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.onValue((v) => {this.props.model.context.changeBus.push(v)})
    let {model} = this.props
    if (model.context.edit) {
      this.validate(model)
    }
  }
})
StringEditor.handlesOptional = true
StringEditor.canShowInline = () => true