import React from 'react'
import * as R from 'ramda'
import {contextualizeModel} from './EditorModel.ts'
import {currentLocation} from '../util/location'
import Text from '../i18n/Text'
import {modelData} from './EditorModel'
import InvalidateOpiskeluoikeusButton from '../opiskeluoikeus/InvalidateOpiskeluoikeusButton'

export class TogglableEditor extends React.Component {
  render() {
    let { model, renderChild } = this.props
    let context = model.context
    let opiskeluoikeusOid = modelData(model.context.opiskeluoikeus, 'oid')
    let edit = opiskeluoikeusOid && currentLocation().params.edit == opiskeluoikeusOid
    let editingAny = !!currentLocation().params.edit
    let modifiedContext = R.mergeRight(context, { edit })
    let showEditLink = model.editable && !editingAny
    let showDeleteLink = model.invalidatable && !showEditLink
    let editLink = showEditLink
      ? <button className="koski-button toggle-edit" onClick={() => context.editBus.push(opiskeluoikeusOid)}><Text name="muokkaa"/></button>
      : showDeleteLink
        ? <InvalidateOpiskeluoikeusButton opiskeluoikeus={model.context.opiskeluoikeus} />
        : null

    return (renderChild(contextualizeModel(model, modifiedContext), editLink))
  }
}
