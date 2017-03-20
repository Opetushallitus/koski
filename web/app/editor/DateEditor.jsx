import React from 'react'
import Bacon from 'baconjs'
import {resetOptionalModel} from './OptionalEditor.jsx'
import {modelTitle, modelSetData} from './EditorModel.js'
import {formatISODate, parseFinnishDate} from '../date.js'

export const DateEditor = React.createClass({
  render() {
    let {model, isValid = () => true } = this.props
    let {invalidDate, valueBus} = this.state

    let onChange = (event) => {
      let date = parseFinnishDate(event.target.value)
      let valid = (model.optional && !event.target.value) || (date && isValid(date))
      if (valid) {
        if (date) {
          valueBus.push([model.context, modelSetData(model, formatISODate(date))])
        } else {
          resetOptionalModel(model)
        }
      }
      model.context.errorBus.push([model.context, {error: !valid}])
      this.setState({invalidDate: !valid})
    }

    return model.context.edit
      ? <input type="text" defaultValue={modelTitle(model)} onChange={ onChange } className={invalidDate ? 'date-editor error' : 'date-editor'}></input>
      : <span className="inline date">{modelTitle(model)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },
  componentDidMount() {
    this.state.valueBus.onValue((v) => {
      console.log("Push date", v)
      this.props.model.context.changeBus.push(v)
    })
  }
})
DateEditor.canShowInline = () => true
DateEditor.handlesOptional = true

