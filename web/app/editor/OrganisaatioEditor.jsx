import React from 'baret'
import {modelData, modelSetValue, pushModel} from './EditorModel'
import OrganisaatioPicker from '../OrganisaatioPicker.jsx'
import {t} from '../i18n'
export const OrganisaatioEditor = ({model, organisaatioTyypit, showAll}) => {
  let canSelectOrg = org => organisaatioTyypit ? org.organisaatiotyypit.some(tyyppi => organisaatioTyypit.includes(tyyppi)) : true
  return model.context.edit
    ? <OrganisaatioPicker
        selectedOrg={ modelData(model) }
        onSelectionChanged = { (org) => pushModel(modelSetValue(model, { data: { oid: org.oid, nimi: org.nimi }, classes: model.value.classes })) }
        canSelectOrg={canSelectOrg}
        clearText=""
        noSelectionText="Valitse..."
        showAll={showAll}
    />
    : <span>{t(modelData(model, 'nimi'))}</span>
}
OrganisaatioEditor.validateModel = (model) => {
  if(!modelData(model, 'oid')) return [{key: 'missing.oid'}]
}