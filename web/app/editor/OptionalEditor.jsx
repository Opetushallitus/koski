import React from 'react'
import * as R from 'ramda'
import { modelEmpty } from './EditorModel.ts'
import { Editor } from './Editor'
import {
  optionalPrototypeModel,
  pushModel,
  resetOptionalModel
} from './EditorModel'
import Text from '../i18n/Text'

export class OptionalEditor extends React.Component {
  render() {
    const { model } = this.props
    const prototype = () => optionalPrototypeModel(model)

    const removeValue = () => {
      resetOptionalModel(this.props.model)
    }
    const addValue = () => {
      pushModel(prototype())
    }

    const modelToBeShown = model
    const empty = modelEmpty(modelToBeShown)

    const canRemove = model.context.edit && !empty

    return (
      <span className="optional-wrapper">
        {empty ? (
          model.context.edit && prototype() ? (
            <a className="add-value" onClick={addValue}>
              <Text name="lisää" />
            </a>
          ) : null
        ) : (
          <Editor model={R.mergeRight(modelToBeShown, { optional: false })} />
        )}
        {canRemove && <a className="remove-value" onClick={removeValue} />}
      </span>
    )
  }
}
OptionalEditor.canShowInline = () => true
