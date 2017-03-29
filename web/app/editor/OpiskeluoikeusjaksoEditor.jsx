import React from 'react'
import {modelTitle} from './EditorModel.js'
import {Editor} from './GenericEditor.jsx'

export const OpiskeluoikeusjaksoEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<div className="opiskeluoikeusjakso">
      <label className="date"><Editor model={model} path="alku"/></label>
      <label className="tila">{modelTitle(model, 'tila')}</label>
    </div>)
  }
})
OpiskeluoikeusjaksoEditor.readOnly = true