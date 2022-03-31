import React from 'baret'
import {modelData, modelSetValue, pushModel} from '../editor/EditorModel'
import OrganisaatioPicker from '../virkailija/OrganisaatioPicker'
import {t} from '../i18n/i18n'
export const OrganisaatioEditor = ({model, organisaatioTyypit, showAll}) => {
  let canSelectOrg = org => organisaatioTyypit ? org.organisaatiotyypit.some(tyyppi => organisaatioTyypit.map(ot => ot.toLowerCase()).includes(tyyppi.toLowerCase())) : true
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
OrganisaatioEditor.displayName = 'OrganisaatioEditor'
