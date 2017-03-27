import R from 'ramda'
import React from 'react'
import Bacon from 'baconjs'
import {optionalModel} from './OptionalEditor.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {addContext, modelLookup, modelSetValue, modelTitle} from './EditorModel.js'

export const LocalizedStringEditor = React.createClass({
  render() {
    let {model} = this.props
    let {valueBus} = this.state
    if (!model.context.edit) {
      return <ObjectEditor model={model}/>
    }

    let stringModel = addContext(modelLookup(model.optional ? optionalModel(model) : model, 'fi'), {changeBus: valueBus})
    return <StringEditor model={model.optional ? R.merge(stringModel, {value: {data: modelTitle(model)}}) : stringModel} />
  },
  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },
  componentDidMount() {
    let {valueBus} = this.state
    valueBus.onValue(([context,model]) => {
      this.props.model.context.changeBus.push([context, model.value.data ? model : modelSetValue(model, undefined)])
    })
  }
})
LocalizedStringEditor.handlesOptional = true
LocalizedStringEditor.canShowInline = () => true
