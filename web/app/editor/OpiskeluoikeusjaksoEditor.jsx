import React from 'react'
import {modelTitle} from './EditorModel.js'
import {Editor} from './Editor.jsx'

export const OpiskeluoikeusjaksoEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<div className={'opiskeluoikeusjakso' + (model.active ? ' active' : '')}>
      <label className="date"><Editor model={model} path="alku" edit={false}/></label>
      <label className="tila">{modelTitle(model, 'tila')}</label>
    </div>)
  }
})
OpiskeluoikeusjaksoEditor.readOnly = true