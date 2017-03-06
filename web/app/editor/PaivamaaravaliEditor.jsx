import React from 'react'
import {Editor} from './GenericEditor.jsx'

export const PäivämääräväliEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<span>
      <Editor model={model} path="alku"/> — <Editor model={model} path="loppu"/>
    </span>)
  }
})
PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.readOnly = true
