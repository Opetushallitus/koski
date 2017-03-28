import React from 'react'
import Bacon from 'baconjs'
import {modelData, modelSetData} from './EditorModel.js'
import {resetOptionalModel} from './OptionalEditor.jsx'

export const StringEditor = React.createClass({
  render() {
    let {model} = this.props
    let {valueBus, error} = this.state

    let onChange = (event) => {
      let err = {error: !model.optional && !event.target.value}
      this.setState(err)
      model.context.errorBus.push([model.context, err])
      let value = event.target.value
      if (!value && model.optional) {
        resetOptionalModel(model)
      } else {
        valueBus.push([model.context, modelSetData(model, value)])
      }
    }

    let data = modelData(model)
    if (typeof data != "string") {
      debugger
    }

    return model.context.edit
      ? <input className={error ? 'error' : 'valid'} type="text" defaultValue={data} onChange={ onChange }></input>
      : <span className="inline string">{!data ? '' : data.split('\n').map((line, k) => <span key={k}>{line}<br/></span>)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.onValue((v) => {this.props.model.context.changeBus.push(v)})
  }
})
StringEditor.handlesOptional = true
StringEditor.canShowInline = () => true