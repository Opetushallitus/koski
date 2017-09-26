import React from 'react'
import R from 'ramda'
import {contextualizeModel} from './EditorModel.js'
import {currentLocation} from '../location'
import Text from '../Text.jsx'
import {contextualizeSubModel, modelData, modelItems, modelLookup, modelSetData, pushModel} from './EditorModel'
import {formatISODate} from '../date'
import {koodistoValues} from '../uusioppija/koodisto'
import {fixOpiskeluoikeudenPäättymispäivä} from './OpiskeluoikeudenTilaEditor.jsx'

export class TogglableEditor extends React.Component {
  render() {
    let { model, renderChild } = this.props
    let context = model.context
    let opiskeluoikeusOid = modelData(model.context.opiskeluoikeus, 'oid')
    let edit = opiskeluoikeusOid && currentLocation().params.edit == opiskeluoikeusOid
    let editingAny = !!currentLocation().params.edit
    let modifiedContext = R.merge(context, { edit })
    let showEditLink = model.editable && !editingAny
    let showDeleteLink = model.editable && editingAny
    let editLink = showEditLink
      ? <button className="toggle-edit" onClick={() => context.editBus.push(opiskeluoikeusOid)}><Text name="muokkaa"/></button>
      : showDeleteLink
        ? <button className="toggle-edit" onClick={() => deleteOpiskeluoikeus(model.context.opiskeluoikeus)}><Text name="poista opiskeluoikeus"/></button>
        : null

    return (renderChild(contextualizeModel(model, modifiedContext), editLink))
  }
}

const deleteOpiskeluoikeus = opiskeluoikeus => {
  let today = formatISODate(new Date())
  let model = tilaListModel(fixOpiskeluoikeudenPäättymispäivä(opiskeluoikeus))

  koodistoValues('koskiopiskeluoikeudentila/mitatoity').map('.0')
    .map(mitätöity => modelSetData(model, mitätöity, 'tila'))
    .map(m => modelSetData(m, today, 'alku'))
    .onValue(pushModel)
}

const tilaListModel = opiskeluoikeus => {
  let model = modelLookup(opiskeluoikeus, 'tila.opiskeluoikeusjaksot')
  return contextualizeSubModel(model.arrayPrototype, model, modelItems(model).length)
}
