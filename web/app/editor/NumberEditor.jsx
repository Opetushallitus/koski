import React from 'react'
import {modelData, pushModelValue} from './EditorModel.js'
import {wrapOptional} from './OptionalEditor.jsx'

export const NumberEditor = React.createClass({
  render() {
    let {model} = this.props
    let wrappedModel = wrapOptional({model})
    let onChange = (event) => pushModelValue(wrappedModel, event.target.value ? { data: parseFloat(event.target.value) } : undefined)

    let data = modelData(wrappedModel)
    let value = data
      ? Math.round(data * 100) / 100
      : data

    return wrappedModel.context.edit
      ? <input type="text" defaultValue={modelData(wrappedModel)} onChange={ onChange } className="editor-input inline number"></input>
      : <span className="inline number">{value}</span>
  }
})

NumberEditor.handlesOptional = true
NumberEditor.validateModel = model => {
  let value = modelData(model)
  return (value !== undefined && isNaN(value)) ? [{key: 'invalid.number'}] : []
}
