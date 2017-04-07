import React from 'baret'
import {modelData, modelSetValue, pushModel} from './EditorModel'
import OrganisaatioPicker from '../OrganisaatioPicker.jsx'

export const ToimipisteEditor = ({model}) => {
  return model.context.edit
    ? <OrganisaatioPicker
        selectedOrg={ modelData(model) }
        onSelectionChanged = { (org) => pushModel(modelSetValue(model, { data: { oid: org.oid, nimi: org.nimi }, classes: model.value.classes })) }
        canSelectOrg={(org) => org.organisaatiotyypit.some(t => t === 'TOIMIPISTE') }
        clearText=""
    />
    : <span>{modelData(model, 'nimi.fi')}</span>
}