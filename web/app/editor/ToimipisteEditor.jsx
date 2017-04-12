import React from 'baret'
import {OrganisaatioEditor} from './OrganisaatioEditor.jsx'

export const ToimipisteEditor = ({model}) => {
  return <div>wat<OrganisaatioEditor model={model} organisaatioTyypit={['TOIMIPISTE']} /></div>
}
ToimipisteEditor.validateModel = OrganisaatioEditor.validateModel