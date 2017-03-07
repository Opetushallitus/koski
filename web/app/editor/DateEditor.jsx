import React from 'react'
import Bacon from 'baconjs'
import {modelTitle} from './EditorModel.js'
import {formatISODate, parseFinnishDate} from '../date.js'

export const DateEditor = React.createClass({
  render() {
    let {model} = this.props
    let {invalidDate, valueBus} = this.state

    let onChange = (event) => {
      var date = parseFinnishDate(event.target.value)
      if (date) {
        valueBus.push([model.context, {data: formatISODate(date)}])
      }
      this.setState({invalidDate: date ? false : true})
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

