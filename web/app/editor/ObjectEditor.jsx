import React from 'react'
import { Editor } from './Editor'
import { PropertiesEditor, shouldShowProperty } from './PropertiesEditor'
import { modelProperties, findModelProperty } from './EditorModel'

export class ObjectEditor extends React.Component {
  render() {
    const { model } = this.props
    const context = model.context
    const className = model.value
      ? 'object ' + model.value.classes.join(' ')
      : 'object empty'
    const representative = findRepresentative(model)
    const representativeEditor = () => <Editor model={representative.model} />
    const objectEditor = () => (
      <div className={className}>
        <PropertiesEditor model={model} />
      </div>
    )

    const exactlyOneVisibleProperty =
      modelProperties(model, shouldShowProperty(context)).length === 1
    const isInline = ObjectEditor.canShowInline(model)
    const objectWrapperClass =
      'foldable-wrapper with-representative' + (isInline ? ' inline' : '')

    return !representative ? (
      objectEditor()
    ) : (exactlyOneVisibleProperty || context.forceInline) && !context.edit ? (
      representativeEditor() // just show the representative property, as it is the only one
    ) : (
      <span className={objectWrapperClass}>{objectEditor()}</span>
    )
  }
}
ObjectEditor.canShowInline = (model) => {
  return !!findRepresentative(model) && !model.context.edit
}

const findRepresentative = (model) =>
  findModelProperty(model, (property) => property.representative)
