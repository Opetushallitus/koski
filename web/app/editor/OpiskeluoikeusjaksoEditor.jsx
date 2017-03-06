import React from 'react'
import {modelTitle} from './EditorModel.js'

export const OpiskeluoikeusjaksoEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<div className="opiskeluoikeusjakso">
      <label className="date">{modelTitle(model, 'alku')}</label>
      <label className="tila">{modelTitle(model, 'tila')}</label>
    </div>)
  }
})
OpiskeluoikeusjaksoEditor.readOnly = true