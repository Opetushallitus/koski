import React from 'react'
import R from 'ramda'
import {contextualizeModel, addContext, contextualizeSubModel, modelLookup} from './EditorModel'
import {parseBool} from '../util'

/*
 model: required model object
 path: optional path to actually used model. It's ok if the model cannot be found by path: a NullEditor will be used
 */
export const Editor = React.createClass({
  render() {
    let { model, ...rest } = this.props
    if (!model) {
      throw new Error('model missing')
    }
    model = Editor.setupContext(model, this.props)
    return getModelEditor(model, rest)
  },

  shouldComponentUpdate(nextProps) {
    return Editor.shouldComponentUpdate.call(this, nextProps)
  }
})

Editor.setupContext = (model, {editorMapping, changeBus, editBus, saveChangesBus, edit, path}) => {
  if (!model.context) {
    if (!editorMapping) {
      console.error('editorMapping required for root editor', model)
      throw new Error('editorMapping required for root editor')
    }
    R.toPairs(editorMapping).forEach(([key, value]) => { if (!value) throw new Error('Editor missing for ' + key) })
    model = contextualizeModel(model, {
      changeBus, saveChangesBus, editBus,
      path: '',
      prototypes: model.prototypes,
      editorMapping
    })
  } else {
    if (!model.context.prototypes) model = addContext(model, { prototypes: model.prototypes })
    if (editorMapping) model = addContext(model, {editorMapping})
    if (changeBus) model = addContext(model, {changeBus})
    if (saveChangesBus) model = addContext(model, {saveChangesBus})
    if (editBus) model = addContext(model, {editBus})
  }
  edit = parseBool(edit)
  if (edit !== model.context.edit) {
    model = addContext(model, { edit })
  }
  if (path) {
    model = modelLookup(model, path)
  }
  return model
}

Editor.shouldComponentUpdate = function(nextProps) {
  if (parseBool(nextProps.alwaysUpdate)) return true
  var next = nextProps.model
  var current = this.props.model
  var result = next.modelId != current.modelId
  if (pathHash(next) !== pathHash(current)) {
    result = true
  }
  if (result) {
    //console.log('update', next.path)
  } else  {
    //console.log('skip', next.path)
  }
  return result
}
let pathHash = (m) => {
  let hash = 0
  for (var i in (m.path || [])) {
    if (typeof m.path[i] == 'number') {
      hash += m.path[i]
    }
  }
  if (m.context && m.context.changeBus) {
    hash += m.context.changeBus.id
  }
  return hash
}
Editor.propTypes = {
  model: React.PropTypes.object.isRequired
}
Editor.canShowInline = (model) => (getEditorFunction(model).canShowInline || (() => false))(model)
Editor.handlesOptional = (model, modifier) => editorFunctionHandlesOptional(getEditorFunction(model), modifier)
const editorFunctionHandlesOptional = (editor, modifier) => editor && (editor.handlesOptional || (() => false))(modifier)

const NullEditor = React.createClass({
  render() {
    return null
  }
})

const getEditorFunction = (model) => { // TODO: refactor this garbage
  let editorByClass = filter => mdl => {
    if (!mdl || !mdl.value) {
      return undefined
    }
    for (var i in mdl.value.classes) {
      var editor = mdl.context.editorMapping[mdl.value.classes[i]]
      if (editor && (!mdl.context.edit || filter(editor)) && (mdl.context.edit || !editor.writeOnly)) { return editor }
    }
  }

  let notReadOnlyEditor = editorByClass(e => !e.readOnly)
  let optionalHandlingEditor = editorByClass(editorFunctionHandlesOptional)

  if (!model) return NullEditor

  if (model.optional) {
    let prototype = model.optionalPrototype && contextualizeSubModel(model.optionalPrototype, model)
    let typeEditor = prototype && model.context.editorMapping[prototype.type]
    return optionalHandlingEditor(prototype) || ((editorFunctionHandlesOptional(typeEditor)) && typeEditor) || model.context.editorMapping.optional
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

const getModelEditor = (model, props) => {
  if (model && !model.context) {
    console.error('Context missing from model', model)
  }
  var ModelEditor = getEditorFunction(model)
  return <ModelEditor model={model} {...props} />
}