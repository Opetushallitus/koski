import React from 'baret'
import {Editor} from '../editor/Editor'
import * as R from 'ramda'
import * as L from 'partial.lenses'
import {hasModelProperty, lensedModel, modelData, modelLookup, modelSetValue} from '../editor/EditorModel'
import {sortLanguages} from '../util/sorting'
import {saveOrganizationalPreference} from '../virkailija/organizationalPreferences'
import {doActionWhileMounted} from '../util/util'
import {isKieliaine, isPaikallinen, koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'
import {t} from '../i18n/i18n'

export class PerusopetuksenOppiaineEditor extends React.Component {
  render() {
    let { oppiaine, showExpand, onExpand, expanded, uusiOppiaineenSuoritus } = this.props
    let äidinkieli = oppiaine.value.classes.includes('aidinkieli')

    return (<span>
    {
      oppiaine.context.edit && isPaikallinen(oppiaine)
        ? <span className="koodi-ja-nimi">
              <span className="koodi"><Editor model={oppiaine} path="tunniste.koodiarvo" placeholder={t('Koodi')}/></span>
              <span className="nimi"><Editor model={fixKuvaus(oppiaine)} path="tunniste.nimi" placeholder={t('Oppiaineen nimi')}/></span>
          </span>
        : showExpand ? <button className={`nimi text-button small ${oppiaine.context.edit && 'edit'}`} onClick={() => onExpand(!expanded)}>{oppiaineTitle(oppiaine)}</button> : <span className="nimi">{oppiaineTitle(oppiaine)}</span>
    }
      {
        // kielivalinta
        isKieliaine(oppiaine) && <span className="value kieli"><Editor model={oppiaine} inline={true} path="kieli" sortBy={!äidinkieli && sortLanguages}/></span>
      }
      {
        this.state && this.state.changed && isPaikallinen(oppiaine) && doActionWhileMounted(oppiaine.context.saveChangesBus, () => {
          let data = modelData(oppiaine)
          let organisaatioOid = modelData(oppiaine.context.toimipiste).oid
          let key = data.tunniste.koodiarvo
          saveOrganizationalPreference(organisaatioOid, paikallinenOppiainePrototype(uusiOppiaineenSuoritus).value.classes[0], key, data)
        })
      }
  </span>)
  }

  componentWillReceiveProps(newProps) {
    let currentData = modelData(this.props.oppiaine)
    let newData = modelData(newProps.oppiaine)
    if (!R.equals(currentData, newData)) {
      this.setState({ changed: true})
    }
  }
}

let fixKuvaus = (oppiaine) => {
  return lensedModel(oppiaine, L.rewrite(m => {
    let nimi = modelLookup(m, 'tunniste.nimi').value
    return hasModelProperty(m, 'kuvaus') ? modelSetValue(m, nimi, 'kuvaus') : m
  }))
}

export const paikallinenOppiainePrototype = (oppiaineenSuoritus) => {
  return koulutusModuuliprototypes(oppiaineenSuoritus).find(isPaikallinen)
}

const oppiaineTitle = aine => {
  let kieliaine = isKieliaine(aine)
  let title = t(modelData(aine, 'tunniste.nimi')) + (kieliaine ? ', ' : '')
  return title
}
