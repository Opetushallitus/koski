import React from 'react'
import {Editor} from './GenericEditor.jsx'
import {contextualizeModel, addContext, modelData} from './EditorModel.js'
import Bacon from 'baconjs'


export const PäivämääräväliEditor = React.createClass({
  render() {
    let { model } = this.props
    let { alkuPäiväBus, loppuPäiväBus, validRange } = this.state
    let usedModel = model.value ? model : model.optional ? contextualizeModel(model.optionalPrototype, model.context) : model

    return (<span className={validRange ? 'date-range' : 'date-range error'}>
      <Editor model={addContext(usedModel, {changeBus: alkuPäiväBus})} path="alku"/> — <Editor model={addContext(usedModel, {changeBus: loppuPäiväBus})} path="loppu"/>
    </span>)
  },
  getInitialState() {
    return {
      alkuPäiväBus: Bacon.Bus(),
      loppuPäiväBus: Bacon.Bus(),
      validRange: true
    }
  },
  componentDidMount() {
    let {alkuPäiväBus, loppuPäiväBus} = this.state
    let {model} = this.props

    let data = ([,p]) => p.data

    let isValidRange = Bacon.combineAsArray(
      alkuPäiväBus.map(data).toProperty(modelData(model, 'alku')),
      loppuPäiväBus.map(data).toProperty(modelData(model, 'loppu'))
    ).filter(model.context.edit).map(([alkuPäivä, loppuPäivä]) => !alkuPäivä || !loppuPäivä || new Date(alkuPäivä) <= new Date(loppuPäivä))

    isValidRange.onValue(valid => {
      model.context.errorBus.push([model.context, {error: !valid}])
      this.setState({validRange: valid})
    })

    alkuPäiväBus.merge(loppuPäiväBus)
      .filter(isValidRange)
      .scan([], (changes, c) => changes.concat(c))
      .onValue(changes => model.context.changeBus.push(changes))
  }
})
PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.handlesOptional = true
