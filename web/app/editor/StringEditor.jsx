import React from 'react'
import Bacon from 'baconjs'
import {modelData, modelSetData} from './EditorModel.js'

export const StringEditor = React.createClass({
  render() {
    let {model} = this.props
    let {valueBus, error} = this.state

    let onChange = (event) => {
      let err = {error: !model.optional && !event.target.value}
      this.setState(err)
      model.context.errorBus.push([model.context, err])
      valueBus.push([model.context, modelSetData(model, event.target.value)])
    }

    let data = modelData(model)
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