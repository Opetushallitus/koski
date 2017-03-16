import React from 'react'
import R from 'ramda'
import { modelLookup, contextualizeModel } from './EditorModel.js'

export const Editor = React.createClass({
  render() {
    let { model, editorMapping, changeBus, errorBus, doneEditingBus, path } = this.props
    if (!model.context) {
      if (!editorMapping) throw new Error('editorMapping required for root editor')
      R.toPairs(editorMapping).forEach(([key, value]) => { if (!value) throw new Error('Editor missing for ' + key) })
      model = contextualizeModel(model, {
        changeBus, errorBus, doneEditingBus,
        root: true,
        rootModel: model,
        path: '',
        prototypes: model.prototypes,
        editorMapping
      })
    }
    return getModelEditor(model, path)
  }
})
Editor.propTypes = {
  model: React.PropTypes.object.isRequired
}
Editor.canShowInline = (component) => (getEditorFunction(component.props.model).canShowInline || (() => false))(component)

export const NullEditor = React.createClass({
  render() {
    return null
  }
})

const getEditorFunction = (model) => {

  let editorByClass = filter => mdl => {
    if (!mdl || !mdl.value) {
      return undefined
    }
    for (var i in mdl.value.classes) {
      var editor = mdl.context.editorMapping[mdl.value.classes[i]]
      if (editor && (!mdl.context.edit || filter(editor))) { return editor }
    }
  }

  let notReadOnlyEditor = editorByClass(e => !e.readOnly)
  let optionalHandlingEditor = editorByClass(e => e.handlesOptional)

  if (!model) return NullEditor

  if (model.optional) {
    let prototype = model.optionalPrototype && contextualizeModel(model.optionalPrototype, model.context)
    let typeEditor = model.optionalPrototype && model.context.editorMapping[model.optionalPrototype.type]
    return optionalHandlingEditor(prototype) || (typeEditor && typeEditor.handlesOptional && typeEditor) || model.context.editorMapping.optional
  }

  let editor = notReadOnlyEditor(model) || model.context.editorMapping[model.type]
  if (!editor) {
    if (!model.type) {
      console.error('Typeless model', model)
    } else {
      console.error('Missing editor for type "' + model.type + '"')
    }
    return NullEditor
  }
  return editor
}

const getModelEditor = (model, path) => {
  if (path) {
    return getModelEditor(modelLookup(model, path))
  }
  if (model && !model.context) {
    console.error('Context missing from model', model)
  }
  var ModelEditor = getEditorFunction(model)
  return <ModelEditor model={model} />
}