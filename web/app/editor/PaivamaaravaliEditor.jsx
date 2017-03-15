import React from 'react'
import {Editor} from './GenericEditor.jsx'
import {contextualizeModel} from './EditorModel.js'


export const PäivämääräväliEditor = React.createClass({
  render() {
    let { model } = this.props
    let usedModel =
      model.value ? model : model.optional ? contextualizeModel(model.optionalPrototype, model.context) : model
    return (<span>
      <Editor model={usedModel} path="alku"/> — <Editor model={usedModel} path="loppu"/>
    </span>)
  }
})
PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.handlesOptional = true
