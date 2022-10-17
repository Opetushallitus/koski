import React from 'baret'
import { OrganisaatioEditor } from './OrganisaatioEditor'

export const ToimipisteEditor = ({ model }) => {
  return (
    <OrganisaatioEditor model={model} organisaatioTyypit={['TOIMIPISTE']} />
  )
}
ToimipisteEditor.validateModel = OrganisaatioEditor.validateModel
