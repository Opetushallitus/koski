import React from 'react'
import {modelData, modelTitle, modelSetValue} from './EditorModel.js'

export const BooleanEditor = React.createClass({
  render() {
    let {model} = this.props
    let onChange = event => {
      var data = event.target.checked
      model.context.changeBus.push([model.context, modelSetValue(model, {data: data, title: data ? 'kyllä' : 'ei'}) ]) // TODO: i18n
    }

    return model.context.edit
      ? <input type="checkbox" defaultChecked={modelData(model)} onChange={ onChange }></input>
      : <span className="inline string">{modelTitle(model)}</span>
  }
})
BooleanEditor.canShowInline = () => true