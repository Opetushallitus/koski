import React from 'react'
import {modelTitle} from './EditorModel.js'
import {PropertiesEditor} from './PropertiesEditor.jsx'

export const KoulutusmoduuliEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<span className="koulutusmoduuli">
      <span className="tunniste">{modelTitle(model, 'tunniste')}</span>
      <span className="diaarinumero">{modelTitle(model, 'perusteenDiaarinumero')}</span>
      <PropertiesEditor model={model} propertyFilter={p => !['tunniste', 'perusteenDiaarinumero', 'pakollinen'].includes(p.key)} />
    </span>)
  }
})
KoulutusmoduuliEditor.readOnly = true
