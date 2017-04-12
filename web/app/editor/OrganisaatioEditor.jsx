import React from 'baret'
import {modelData, modelSetValue, pushModel} from './EditorModel'
import OrganisaatioPicker from '../OrganisaatioPicker.jsx'

export const OrganisaatioEditor = ({model, organisaatioTyypit}) => {
  let canSelectOrg = org => organisaatioTyypit ? org.organisaatiotyypit.some(t => organisaatioTyypit.includes(t)) : true
  return model.context.edit
    ? <OrganisaatioPicker
        selectedOrg={ modelData(model) }
        onSelectionChanged = { (org) => pushModel(modelSetValue(model, { data: { oid: org.oid, nimi: org.nimi }, classes: model.value.classes })) }
        canSelectOrg={canSelectOrg}
        clearText=""
    />
    : <span>{modelData(model, 'nimi.fi')}</span>
}
OrganisaatioEditor.validateModel = (model) => {
  if(!modelData(model, 'oid')) return ['Organisaatio puuttuu']
}