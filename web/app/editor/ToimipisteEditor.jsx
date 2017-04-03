import React from 'baret'
import {modelData, modelSetValue} from './EditorModel'
import OrganisaatioPicker from '../OrganisaatioPicker.jsx'

export const ToimipisteEditor = ({model}) => {
  let onSelectionChanged = (org) => {
    let updatedModel = modelSetValue(model, { data: { oid: org.oid, nimi: org.nimi }, classes: model.value.classes })
    model.context.changeBus.push([model.context, updatedModel])
  }
  return model.context.edit
    ? <OrganisaatioPicker selectedOrg={ modelData(model) } onSelectionChanged = { onSelectionChanged } />
    : <span>{modelData(model, 'nimi.fi')}</span>
}