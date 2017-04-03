import React from 'react'
import {modelData, modelSetValue} from './EditorModel.js'

export const BooleanEditor = React.createClass({
  render() {
    let {model} = this.props
    let localizedBoolean = (b) => b ? 'kyllä' : 'ei' // TODO: i18n
    let onChange = event => {
      var data = event.target.checked
      model.context.changeBus.push([model.context, modelSetValue(model, {data: data}) ])
    }

    return model.context.edit
      ? <input type="checkbox" className="editor-input" defaultChecked={modelData(model)} onChange={ onChange }></input>
      : <span className="inline string">{localizedBoolean(modelData(model))}</span>
  }
})
BooleanEditor.canShowInline = () => true