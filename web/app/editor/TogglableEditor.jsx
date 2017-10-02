import React from 'react'
import R from 'ramda'
import {contextualizeModel} from './EditorModel.js'
import {currentLocation} from '../location'
import Text from '../Text.jsx'
import {modelData, modelItems} from './EditorModel'
import {invalidateOpiskeluoikeus} from '../Oppija.jsx'

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
        ? <MitätöiButton opiskeluoikeus={model.context.opiskeluoikeus} />
        : null

    return (renderChild(contextualizeModel(model, modifiedContext), editLink))
  }
}

class MitätöiButton extends React.Component {
  render() {
    let { opiskeluoikeus } = this.props
    let deleteRequested = this.state && this.state.deleteRequested
    let mitätöi = () => {
      if (deleteRequested) {
        invalidateOpiskeluoikeus(modelData(opiskeluoikeus, 'oid'))
      } else {
        this.setState({deleteRequested: true})
      }
    }

    return suorituksiaTehty(opiskeluoikeus)
      ? null
      : <button className={deleteRequested ? 'invalidate confirm' : 'invalidate'} onClick={mitätöi}><Text name={deleteRequested ? 'Vahvista mitätöinti, operaatiota ei voi peruuttaa' : 'Mitätöi opiskeluoikeus'}/></button>
  }
}

const suorituksiaTehty = opiskeluoikeus => {
  let suoritukset = modelItems(opiskeluoikeus, 'suoritukset')
  let osasuoritukset = suoritukset.flatMap(s => modelItems(s, 'osasuoritukset'))
  return osasuoritukset.find(s => modelData(s, 'tila').koodiarvo === 'VALMIS') !== undefined
}

