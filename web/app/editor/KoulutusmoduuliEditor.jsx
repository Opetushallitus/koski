import React from 'react'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {Editor} from './Editor.jsx'
import {t} from '../i18n.js'

export class KoulutusmoduuliEditor extends React.Component {
  render() {
    let { model } = this.props
    let overrideEdit = model.context.editAll ? true : false
    return (<span className="koulutusmoduuli">
      <span className="tunniste"><Editor model={model} path="tunniste" edit={overrideEdit}/></span>
      <span className="diaarinumero"><span className="value">
        <Editor model={model} path="perusteenDiaarinumero" placeholder={t('Perusteen diaarinumero')}/>
      </span></span>
      <PropertiesEditor model={model} propertyFilter={p => !['tunniste', 'perusteenDiaarinumero', 'pakollinen'].includes(p.key)} />
    </span>)
  }
}
