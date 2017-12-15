import React from 'react'
import { Editor } from './Editor'
import { PropertiesEditor, shouldShowProperty } from './PropertiesEditor'
import {modelProperties} from './EditorModel'
import {findModelProperty} from './EditorModel'

export class ObjectEditor extends React.Component {
  render() {
    let {model} = this.props
    let context = model.context
    let className = model.value
      ? 'object ' + model.value.classes.join(' ')
      : 'object empty'
    let representative = findRepresentative(model)
    let representativeEditor = () => <Editor model={representative.model}/>
    let objectEditor = () => <div className={className}><PropertiesEditor model={model}/></div>

    let exactlyOneVisibleProperty = modelProperties(model, shouldShowProperty(context)).length === 1
    let isInline = ObjectEditor.canShowInline(model)
    let objectWrapperClass = 'foldable-wrapper with-representative' + (isInline ? ' inline' : '')

    return !representative
      ? objectEditor()
      : ((exactlyOneVisibleProperty || context.forceInline) && !context.edit)
        ? representativeEditor() // just show the representative property, as it is the only one
        : <span className={objectWrapperClass}>{objectEditor()}</span>
  }
}
ObjectEditor.canShowInline = (model) => {
  return !!findRepresentative(model) && !model.context.edit
}

const findRepresentative = (model) => findModelProperty(model, property => property.representative)