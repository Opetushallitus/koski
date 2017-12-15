import React from 'react'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {Editor} from '../editor/Editor'
import {t} from '../i18n/i18n.js'
import {suorituksenTyyppi} from './Suoritus'
import {buildClassNames} from '../components/classnames'

export class KoulutusmoduuliEditor extends React.Component {
  render() {
    let { model } = this.props
    let overrideEdit = model.context.editAll ? true : false
    let suoritusTyyppi = model.context.suoritus && suorituksenTyyppi(model.context.suoritus)
    let propertyFilter  = p => {
      let excludedProperties = ['tunniste', 'perusteenDiaarinumero', 'pakollinen']
      let esiopetusKuvaus = suoritusTyyppi === 'esiopetuksensuoritus' && p.key === 'kuvaus'
      return !excludedProperties.includes(p.key) && !esiopetusKuvaus
    }
    return (<span className="koulutusmoduuli">
      <span className="tunniste">
        {
          ['aikuistenperusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaaranalkuvaihe'].includes(suoritusTyyppi)
            ? <Editor model={model.context.suoritus} path="tyyppi" edit={false}/>
            : <Editor model={model} path="tunniste" edit={overrideEdit}/>
        }
      </span>
      <span className="diaarinumero"><span className={buildClassNames(['value', !model.context.edit && 'label'])}>
        <Editor model={model} path="perusteenDiaarinumero" placeholder={t('Perusteen diaarinumero')} />
      </span></span>
      <PropertiesEditor model={model} propertyFilter={propertyFilter} />
    </span>)
  }
}