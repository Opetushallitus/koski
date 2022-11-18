import React from 'react'
import * as R from 'ramda'
import { contextualizeModel } from './EditorModel.ts'
import { currentLocation } from '../util/location'
import Text from '../i18n/Text'
import { modelData } from './EditorModel'
import InvalidateOpiskeluoikeusButton from '../opiskeluoikeus/InvalidateOpiskeluoikeusButton'

export class TogglableEditor extends React.Component {
  render() {
    const { model, renderChild, ...rest } = this.props
    const context = model.context
    const opiskeluoikeusOid = modelData(model.context.opiskeluoikeus, 'oid')
    const edit =
      opiskeluoikeusOid && currentLocation().params.edit == opiskeluoikeusOid
    const editingAny = !!currentLocation().params.edit
    const modifiedContext = R.mergeRight(context, { edit })
    const showEditLink = model.editable && !editingAny
    const showDeleteLink = model.invalidatable && !showEditLink
    const editLink = showEditLink ? (
      <button
        className="koski-button toggle-edit"
        onClick={() => context.editBus.push(opiskeluoikeusOid)}
        {...rest}
      >
        <Text name="muokkaa" />
      </button>
    ) : showDeleteLink ? (
      <InvalidateOpiskeluoikeusButton
        opiskeluoikeus={model.context.opiskeluoikeus}
      />
    ) : null

    return renderChild(contextualizeModel(model, modifiedContext), editLink)
  }
}
