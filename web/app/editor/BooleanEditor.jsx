import React from 'react'
import {modelData, modelSetValue} from './EditorModel.js'
import {pushModel} from './EditorModel'
import {t} from '../i18n.js'

export const BooleanEditor = React.createClass({
  render() {
    let {model} = this.props
    let localizedBoolean = (b) => b ? t('kyllä') : t('ei')
    let onChange = event => {
      var data = event.target.checked
      pushModel(modelSetValue(model, {data: data}))
    }

    return model.context.edit
      ? <input type="checkbox" className="editor-input" defaultChecked={modelData(model)} onChange={ onChange }></input>
      : <span className="inline string">{localizedBoolean(modelData(model))}</span>
  }
})
BooleanEditor.canShowInline = () => true