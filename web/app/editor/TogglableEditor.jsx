import React from 'react'
import R from 'ramda'
import {contextualizeModel} from './EditorModel.js'
import {modelValid} from './EditorModel'
import {navigateWithQueryParams, currentLocation} from '../location'
import {parseBool} from '../util'

export const TogglableEditor = React.createClass({
  render() {
    let { model, renderChild } = this.props
    let context = model.context
    let hasErrors = !modelValid(model)
    let edit = context.edit || parseBool(currentLocation().params.edit)

    let toggleEdit = () => {
      if (edit) {
        context.doneEditingBus.push()
      }
      navigateWithQueryParams({edit: !edit ? 'true' : undefined})
    }
    let showToggleEdit = model.editable && !context.edit && !context.hasToggleEdit
    let modifiedContext = R.merge(context, {
      edit: edit,
      hasToggleEdit: context.hasToggleEdit || showToggleEdit  // to prevent nested duplicate "edit" links
    })
    let editLink = showToggleEdit
      ? hasErrors && edit
        ? <span className="toggle-edit disabled">valmis</span>
        : <a className={edit ? 'toggle-edit editing' : 'toggle-edit'} onClick={toggleEdit}>{edit ? 'valmis' : 'muokkaa'}</a>
      : null

    return (renderChild(contextualizeModel(model, modifiedContext), editLink))
  }
})
