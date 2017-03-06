import React from 'react'
import {modelData} from './EditorModel.js'

export const NumberEditor = React.createClass({
  render() {
    let {model} = this.props
    let onChange = (event) => model.context.changeBus.push([model.context, {data: parseFloat(event.target.value)}])

    let data = modelData(model)
    let value = data
      ? Math.round(data * 100) / 100
      : data

    return model.context.edit
      ? <input type="text" defaultValue={modelData(model)} onChange={ onChange } className="inline number"></input>
      : <span className="inline number">{value}</span>
  }
})
