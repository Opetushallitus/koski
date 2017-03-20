import React from 'react'
import {modelData, modelTitle, modelSetData} from './EditorModel.js'

export const BooleanEditor = React.createClass({
  render() {
    let {model} = this.props
    let onChange = event => {
      model.context.changeBus.push([model.context, modelSetData(model, event.target.checked) ])
    }

    return model.context.edit
      ? <input type="checkbox" defaultChecked={modelData(model)} onChange={ onChange }></input>
      : <span className="inline string">{modelTitle(model)}</span>
  }
})
BooleanEditor.canShowInline = () => true