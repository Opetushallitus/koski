import React from 'react'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {Editor} from './Editor.jsx'

export const KoulutusmoduuliEditor = React.createClass({
  render() {
    let { model } = this.props
    let overrideEdit = model.context.editAll ? true : false
    return (<span className="koulutusmoduuli">
      <span className="tunniste"><Editor model={model} path="tunniste" edit={overrideEdit}/></span>
      <span className="diaarinumero">
        <Editor model={model} path="perusteenDiaarinumero" placeholder="Perusteen diaarinumero"/>
      </span>
      <PropertiesEditor model={model} propertyFilter={p => !['tunniste', 'perusteenDiaarinumero', 'pakollinen'].includes(p.key)} />
    </span>)
  }
})
