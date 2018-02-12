import React from 'baret'
import {Editor} from '../editor/Editor'
import {PropertiesEditor, shouldShowProperty} from '../editor/PropertiesEditor'
import {modelData, modelEmpty, modelErrorMessages, modelLookup, modelProperties, pushRemoval} from '../editor/EditorModel'
import {PerusopetuksenOppiaineEditor} from './PerusopetuksenOppiaineEditor'
import {isPaikallinen} from '../suoritus/Koulutusmoduuli'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {tilaKoodi} from '../suoritus/Suoritus'
import {FootnoteHint} from '../components/footnote'

export class PerusopetuksenOppiaineRowEditor extends React.Component {
  render() {
    let {model, showArvosana, showLaajuus, footnotes, uusiOppiaineenSuoritus, expanded, onExpand} = this.props
    const {edit} = model.context

    let oppiaine = modelLookup(model, 'koulutusmoduuli')
    let className = 'oppiaine oppiaine-rivi'
      + ' ' + (modelData(model, 'koulutusmoduuli.pakollinen') ? 'pakollinen' : 'valinnainen')
      + ' ' + modelData(oppiaine, 'tunniste').koodiarvo
      + ' ' + tilaKoodi(model)
      + (expanded ? ' expanded' : '')
      + (isPaikallinen(oppiaine) ? ' paikallinen' : '')


    let extraProperties = expandableProperties(model)

    let showExpand = extraProperties.length > 0

    let sanallinenArvioProperties = modelProperties(modelLookup(model, 'arviointi.-1'), p => p.key === 'kuvaus')
    let showSanallinenArvio = (model.context.edit && sanallinenArvioProperties.length > 0) || sanallinenArvioProperties.filter(p => !modelEmpty(p.model)).length > 0

    return (<tbody className={className}>
    <tr>
      <td className="oppiaine">
        { // expansion link
          showExpand && <a className="toggle-expand" onClick={() => onExpand(!expanded)}>{ expanded ? '' : ''}</a>
        }
        <PerusopetuksenOppiaineEditor {...{oppiaine, showExpand, expanded, onExpand, uusiOppiaineenSuoritus}}/>

      </td>
      {
        showArvosana && <td className="arvosana">
          <span className="value"><ArvosanaEditor model={model}/></span>
        </td>
      }
      {
        showLaajuus && (<td className="laajuus">
          <Editor model={model} path="koulutusmoduuli.laajuus" compact="true"/>
        </td>)
      }
      {
        !edit && footnotes && footnotes.length > 0 && (
          <td className="footnotes">
            <div className="footnotes-container">
              {footnotes.map(note => <FootnoteHint key={note.hint} title={note.title} hint={note.hint} />)}
            </div>
          </td>
        )
      }
      {
        edit && (
          <td>
            <a className="remove-value" onClick={() => pushRemoval(model)}/>
          </td>
        )
      }
    </tr>
    {
      showSanallinenArvio && <tr key='sanallinenArviointi' className="sanallinen-arviointi"><td colSpan="4" className="details"><PropertiesEditor properties={sanallinenArvioProperties} context={model.context} /></td></tr>
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
    if (['koulutusmoduuli', 'arviointi', 'tunniste', 'kieli', 'laajuus', 'pakollinen', 'arvosana', 'päivä', 'perusteenDiaarinumero', 'osasuoritukset'].includes(p.key)) return false // these are never shown
    return shouldShowProperty(model.context)(p)
  }

  return modelProperties(oppiaine)
    .concat(modelProperties(model))
    .filter(extraPropertiesFilter)
}
