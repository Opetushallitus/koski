import React from 'baret'
import {Editor} from './Editor.jsx'
import R from 'ramda'
import * as L from 'partial.lenses'
import {modelData, lensedModel, modelLookup, hasModelProperty, modelSetValue, oneOfPrototypes} from './EditorModel'
import {sortLanguages} from '../sorting'
import {saveOrganizationalPreference} from '../organizationalPreferences'
import {doActionWhileMounted} from '../util'
import {isPaikallinen} from './Koulutusmoduuli'

export const PerusopetuksenOppiaineEditor = React.createClass({
  render() {
    let { oppiaine, showExpand, toggleExpand, uusiOppiaineenSuoritus } = this.props
    let oppiaineTitle = (aine) => {
      let title = modelData(aine, 'tunniste.nimi').fi + (kielenOppiaine || äidinkieli ? ', ' : '')
      return pakollinen === false ? 'Valinnainen ' + title.toLowerCase() : title
    }
    let pakollinen = modelData(oppiaine, 'pakollinen')
    let kielenOppiaine = oppiaine.value.classes.includes('peruskoulunvierastaitoinenkotimainenkieli')
    let äidinkieli = oppiaine.value.classes.includes('peruskoulunaidinkielijakirjallisuus')

    return (<span>
    {
      oppiaine.context.edit && isPaikallinen(oppiaine)
        ? <span className="koodi-ja-nimi">
              <span className="koodi"><Editor model={oppiaine} path="tunniste.koodiarvo" placeholder="Koodi"/></span>
              <span className="nimi"><Editor model={fixKuvaus(oppiaine)} path="tunniste.nimi" placeholder="Oppiaineen nimi"/></span>
          </span>
        : showExpand ? <a className="nimi" onClick={toggleExpand}>{oppiaineTitle(oppiaine)}</a> : <span className="nimi">{oppiaineTitle(oppiaine)}</span>
    }
      {
        // kielivalinta
        (kielenOppiaine || äidinkieli) && <span className="value kieli"><Editor model={oppiaine} path="kieli" sortBy={kielenOppiaine && sortLanguages}/></span>
      }
      {
        this.state && this.state.changed && isPaikallinen(oppiaine) && doActionWhileMounted(oppiaine.context.doneEditingBus, () => {
          let data = modelData(oppiaine)
          let organisaatioOid = modelData(oppiaine.context.toimipiste).oid
          let key = data.tunniste.koodiarvo
          saveOrganizationalPreference(organisaatioOid, paikallinenOppiainePrototype(uusiOppiaineenSuoritus).value.classes[0], key, data)
        })
      }
  </span>)
  },

  componentWillReceiveProps(newProps) {
    let currentData = modelData(this.props.oppiaine)
    let newData = modelData(newProps.oppiaine)
    if (!R.equals(currentData, newData)) {
      this.setState({ changed: true})
    }
  }
})

let fixKuvaus = (oppiaine) => {
  return lensedModel(oppiaine, L.rewrite(m => {
    let nimi = modelLookup(m, 'tunniste.nimi').value
    return hasModelProperty(m, 'kuvaus') ? modelSetValue(m, nimi, 'kuvaus') : m
  }))
}

export const paikallinenOppiainePrototype = (oppiaineenSuoritus) => koulutusModuuliprototypes(oppiaineenSuoritus).find(isPaikallinen)
export const koulutusModuuliprototypes = (oppiaineenSuoritus) => oneOfPrototypes(modelLookup(oppiaineenSuoritus, 'koulutusmoduuli'))