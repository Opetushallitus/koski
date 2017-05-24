import React from 'react'
import R from 'ramda'
import {contextualizeModel} from './EditorModel.js'
import {currentLocation} from '../location'
import Text from '../Text.jsx'

export const TogglableEditor = React.createClass({
  render() {
    let { model, renderChild } = this.props
    let context = model.context
    let opiskeluoikeusId = model.context.opiskeluoikeusId
    let edit = opiskeluoikeusId && currentLocation().params.edit == opiskeluoikeusId
    let editingAny = !!currentLocation().params.edit
    let modifiedContext = R.merge(context, { edit })
    let editLink = model.editable && !editingAny
      ? <button className="toggle-edit" onClick={() => context.editBus.push(opiskeluoikeusId)}><Text name="muokkaa"/></button>
      : null

    return (renderChild(contextualizeModel(model, modifiedContext), editLink))
  }
})
