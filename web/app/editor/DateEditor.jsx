import React from 'react'
import Bacon from 'baconjs'
import {modelTitle} from './EditorModel.js'
import {formatISODate, parseFinnishDate} from '../date.js'

export const DateEditor = React.createClass({
  render() {
    let {model, isValid = (d) => true } = this.props
    let {invalidDate, valueBus} = this.state

    let onChange = (event) => {
      let date = parseFinnishDate(event.target.value)
      let valid = date && isValid(date)
      if (valid) {
        valueBus.push([model.context, {data: formatISODate(date)}])
      }
      model.context.errorBus.push([model.context, {error: !valid}])
      this.setState({invalidDate: !valid})
    }

    return model.context.edit
      ? <input type="text" defaultValue={modelTitle(model)} onChange={ onChange } className={invalidDate ? 'error' : ''}></input>
      : <span className="inline date">{modelTitle(model)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.onValue((v) => {this.props.model.context.changeBus.push(v)})
  }
})
DateEditor.canShowInline = () => true

