import R from 'ramda'
import React from 'react'
import Bacon from 'baconjs'
import {optionalModel} from './OptionalEditor.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {addContext, modelLookup, modelData, modelSetValue, modelSet, modelTitle} from './EditorModel.js'
import {resetOptionalModel} from './OptionalEditor.jsx'

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
    let {model} = this.props
    let {valueBus} = this.state
    valueBus.onValue(([context,stringModel]) => {
      if (!modelData(stringModel)) {
        resetOptionalModel(model)
      } else {
        let myModel = model.optional ? optionalModel(model) : model
        let subpath = context.path.substring(model.context.path.length + 1)
        let updatedModel = modelSet(myModel, stringModel, subpath)
        model.context.changeBus.push([model.context, updatedModel])
      }
    })
  }
})
LocalizedStringEditor.handlesOptional = true
LocalizedStringEditor.canShowInline = () => true
