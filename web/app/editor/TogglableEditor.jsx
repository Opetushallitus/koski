import React from 'react'
import R from 'ramda'
import {contextualizeModel} from './EditorModel.js'
import {currentLocation} from '../location'
import Text from '../Text.jsx'
import {modelData} from './EditorModel'

export class TogglableEditor extends React.Component {
  render() {
    let { model, renderChild } = this.props
    let context = model.context
    let opiskeluoikeusOid = modelData(model.context.opiskeluoikeus, 'oid')
    let edit = opiskeluoikeusOid && currentLocation().params.edit == opiskeluoikeusOid
    let editingAny = !!currentLocation().params.edit
    let modifiedContext = R.merge(context, { edit })
    let editLink = model.editable && !editingAny
      ? <button className="toggle-edit" onClick={() => context.editBus.push(opiskeluoikeusOid)}><Text name="muokkaa"/></button>
      : null

    return (renderChild(contextualizeModel(model, modifiedContext), editLink))
  }
}
