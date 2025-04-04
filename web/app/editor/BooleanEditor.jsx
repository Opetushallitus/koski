import React from 'react'
import { modelData, modelSetValue } from './EditorModel.ts'
import { pushModel } from './EditorModel'
import Text from '../i18n/Text'

export class BooleanEditor extends React.Component {
  render() {
    const { model } = this.props
    const onChange = (event) => {
      const data = event.target.checked
      pushModel(modelSetValue(model, { data }))
    }

    return model.context.edit ? (
      <input
        type="checkbox"
        className="editor-input"
        defaultChecked={modelData(model)}
        onChange={onChange}
      ></input>
    ) : (
      <span className="inline string">
        <Text name={modelData(model) ? 'kyllä' : 'ei'} />
      </span>
    )
  }
}
BooleanEditor.canShowInline = () => true
BooleanEditor.handlesOptional = () => true
