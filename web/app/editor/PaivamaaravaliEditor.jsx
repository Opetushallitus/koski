import React from 'react'
import {Editor} from './Editor.jsx'
import {contextualizeModel, addContext, modelData, modelLookup, childContext, modelSet} from './EditorModel.js'
import {resetOptionalModel} from './OptionalEditor.jsx'
import Bacon from 'baconjs'
import {modelEmpty, modelValid} from './EditorModel'

export const PäivämääräväliEditor = React.createClass({
  render() {
    let { alkuPäiväBus, loppuPäiväBus } = this.state
    var model = this.getUsedModel()
    let validRange = modelValid(model)
    return (<span className={validRange ? 'date-range' : 'date-range error'}>
      <Editor model={addContext(model, {changeBus: alkuPäiväBus})} path="alku"/> — <Editor model={addContext(model, {changeBus: loppuPäiväBus})} path="loppu"/>
    </span>)
  },
  getInitialState() {
    return {
      alkuPäiväBus: Bacon.Bus(),
      loppuPäiväBus: Bacon.Bus()
    }
  },
  getUsedModel() {
    let { model } = this.props
    return model.value ? model : model.optional ? contextualizeModel(model.optionalPrototype, model.context) : model
  },
  componentDidMount() {
    let {alkuPäiväBus, loppuPäiväBus} = this.state
    let {model} = this.props

    let initialChangeEventFromModel = (path) => {
      return [childContext(this.getUsedModel().context, path), modelLookup(this.getUsedModel(), path)]
    }

    let rangeP = Bacon.combineTemplate({
      alku: alkuPäiväBus.toProperty(initialChangeEventFromModel('alku')),
      loppu: loppuPäiväBus.toProperty(initialChangeEventFromModel('loppu'))
    })

    rangeP.changes().onValue(({alku, loppu}) => {
      if (modelEmpty(alku[1]) && modelEmpty(loppu[1]) && model.optional) {
        resetOptionalModel(model)
      } else {
        let withAlku = modelSet(this.getUsedModel(), alku[1], 'alku')
        let withLoppu = modelSet(withAlku, loppu[1], 'loppu')
        let context = model.context
        var values = [context, withLoppu]
        model.context.changeBus.push(values)
      }
    })
  }
})
PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.handlesOptional = true
PäivämääräväliEditor.validateModel = (model) => {
  let alkuData = modelData(model, 'alku')
  let loppuData = modelData(model, 'loppu')
  if (!alkuData || !loppuData || new Date(alkuData) <= new Date(loppuData)) return
  return ['invalid range']
}