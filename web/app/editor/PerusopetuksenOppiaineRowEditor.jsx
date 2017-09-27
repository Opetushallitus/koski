import React from 'baret'
import Bacon from 'baconjs'
import {Editor} from './Editor.jsx'
import {PropertiesEditor, shouldShowProperty} from './PropertiesEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {modelData, modelErrorMessages, modelLookup, modelProperties, modelSetValue, pushRemoval} from './EditorModel'
import {PerusopetuksenOppiaineEditor} from './PerusopetuksenOppiaineEditor.jsx'
import {isPaikallinen} from './Koulutusmoduuli'
import {t} from '../i18n'
import {KurssitEditor} from './KurssitEditor.jsx'
import {ArvosanaEditor} from './ArvosanaEditor.jsx'

export class PerusopetuksenOppiaineRowEditor extends React.Component {
  render() {
    let {model, showLaajuus, showFootnotes, uusiOppiaineenSuoritus, expanded, onExpand} = this.props

    let oppiaine = modelLookup(model, 'koulutusmoduuli')
    let className = 'oppiaine oppiaine-rivi'
      + ' ' + (modelData(model, 'koulutusmoduuli.pakollinen') ? 'pakollinen' : 'valinnainen')
      + ' ' + modelData(oppiaine, 'tunniste').koodiarvo
      + ' ' + modelData(model, 'tila.koodiarvo').toLowerCase()
      + (expanded ? ' expanded' : '')
      + (isPaikallinen(oppiaine) ? ' paikallinen' : '')


    let extraProperties = expandableProperties(model)

    let showExpand = extraProperties.length > 0

    let sanallinenArvioProperties = modelProperties(modelLookup(model, 'arviointi.-1'), p => p.key == 'kuvaus')

    return (<tbody className={className}>
    <tr>
      <td className="oppiaine">
        { // expansion link
          showExpand && <a className="toggle-expand" onClick={() => onExpand(!expanded)}>{ expanded ? '' : ''}</a>
        }
        <PerusopetuksenOppiaineEditor {...{oppiaine, showExpand, expanded, onExpand, uusiOppiaineenSuoritus}}/>

      </td>
      <td className="arvosana">
        <span className="value"><ArvosanaEditor model={ model } /></span>
      </td>
      {
        showLaajuus && (<td className="laajuus">
          <Editor model={model} path="koulutusmoduuli.laajuus" compact="true"/>
        </td>)
      }
      {
        showFootnotes && (
          <td className="footnotes">
            <div className="footnotes-container">
              {modelData(model, 'yksilöllistettyOppimäärä') ? <sup className="yksilollistetty" title={t('Yksilöllistetty oppimäärä')}>{' *'}</sup> : null}
              {modelData(model, 'painotettuOpetus') ? <sup className="painotettu" title={t('Painotettu opetus')}>{' **'}</sup> : null}
              {modelData(model, 'korotus') ? <sup className="korotus" title={t('Perusopetuksen päättötodistuksen arvosanan korotus')}>{' †'}</sup> : null}
            </div>
          </td>
        )
      }
      {
        model.context.edit && (
          <td>
            <a className="remove-value" onClick={() => pushRemoval(model)}/>
          </td>
        )
      }
    </tr>
    {
      sanallinenArvioProperties.length > 0 && <tr key='sanallinenArviointi' className="sanallinen-arviointi"><td colSpan="4" className="details"><PropertiesEditor properties={sanallinenArvioProperties} context={model.context} /></td></tr>
    }
    {
      expanded && <tr key='details'><td colSpan="4" className="details"><PropertiesEditor context={model.context} properties={extraProperties} /></td></tr>
    }
    <tr className="kurssit"><td><KurssitEditor model={model}/></td></tr>
    {
      modelErrorMessages(model).map((error, i) => <tr key={'error-' + i} className="error"><td colSpan="42" className="error">{error}</td></tr>)
    }
    </tbody>)
  }
}

export const expandableProperties = (model) => {
  let edit = model.context.edit
  let oppiaine = modelLookup(model, 'koulutusmoduuli')

  let extraPropertiesFilter = p => {
    if (!edit && ['yksilöllistettyOppimäärä', 'painotettuOpetus', 'suorituskieli', 'korotus'].includes(p.key)) return false // these are only shown when editing
    if (['koulutusmoduuli', 'arviointi', 'tila', 'tunniste', 'kieli', 'laajuus', 'pakollinen', 'arvosana', 'päivä', 'perusteenDiaarinumero', 'osasuoritukset'].includes(p.key)) return false // these are never shown
    return shouldShowProperty(model.context)(p)
  }

  return modelProperties(oppiaine)
    .concat(modelProperties(model))
    .filter(extraPropertiesFilter)
}

// TODO: move this to a more appropriate place. Maybe rename?
export const completeWithFieldAlternatives = (models, path) => {
  const alternativesForField = (model) => EnumEditor.fetchAlternatives(modelLookup(model, path))
    .map(alternatives => alternatives.map(enumValue => modelSetValue(model, enumValue, path)))
  return Bacon.combineAsArray(models.map(alternativesForField)).last().map(x => x.flatten())
}