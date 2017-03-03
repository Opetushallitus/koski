import React from 'react'
import { modelData, modelLookup, modelTitle, modelItems } from './EditorModel.js'
import { Editor } from './GenericEditor.jsx'
import { LaajuusEditor, JaksoEditor } from './Editors.jsx'
import R from 'ramda'

export const PerusopetuksenOppiaineetEditor = React.createClass({
  render() {
    let {model} = this.props
    let käyttäytymisenArvioModel = modelLookup(model, 'käyttäytymisenArvio')
    let grouped = R.toPairs(R.groupBy((o => modelData(o).koulutusmoduuli.pakollinen ? 'Pakolliset oppiaineet' : 'Valinnaiset oppiaineet'), modelItems(model, 'osasuoritukset') || []))

    let osasuoritukset = modelItems(model, 'osasuoritukset') || []
    let korotus = osasuoritukset.find(s => modelData(s, 'korotus')) ? ['† = perusopetuksen päättötodistuksen arvosanan korotus'] : []
    let yksilöllistetty = osasuoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä')) ? ['* = yksilöllistetty oppimäärä'] : []
    let painotettu = osasuoritukset.find(s => modelData(s, 'painotettuOpetus')) ? ['** = painotettu opetus'] : []
    let selitteet = korotus.concat(yksilöllistetty).concat(painotettu).join(', ')

    return grouped.length > 0 && (<div className="oppiaineet">
        <h5>Oppiaineiden arvosanat</h5>
        <p>Arvostelu 4-10, S (suoritettu), H (hylätty) tai V (vapautettu)</p>
        {
          grouped.map(([name, suoritukset], i) => (<section key={i}>
              { grouped.length > 1 && <h5>{name}</h5> }
              <Oppiainetaulukko model={model} suoritukset={suoritukset} />
              {
                käyttäytymisenArvioModel && (model.context.edit || modelData(käyttäytymisenArvioModel)) && (i == grouped.length - 1) && (<div>
                  <h5 className="kayttaytyminen">Käyttäytymisen arviointi</h5>
                  {
                    <Editor model={modelLookup(model, 'käyttäytymisenArvio')}/>
                  }
                </div>)
              }
            </section>
          ))
        }
        {selitteet && <p className="selitteet">{selitteet}</p>}
      </div>)
  }
})

const Oppiainetaulukko = React.createClass({
  render() {
    let {suoritukset} = this.props
    let showLaajuus = !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus'))
    let showExpand = !!suoritukset.find(s => modelData(s, 'arviointi.-1.kuvaus'))
    return (<table>
        <thead><tr><th className="oppiaine">Oppiaine</th><th className="arvosana">Arvosana</th>{showLaajuus && <th className="laajuus">Laajuus</th>}</tr></thead>
        {
          suoritukset.map((oppiaine, i) => (<OppiaineEditor key={i} model={oppiaine} showLaajuus={showLaajuus} showExpand={showExpand}/> ))
        }
      </table>
    )
  }
})

const OppiaineEditor = React.createClass({
  render() {
    let {model, showLaajuus, showExpand} = this.props
    let {expanded} = this.state
    var oppiaine = modelTitle(model, 'koulutusmoduuli')
    let sanallinenArviointi = modelTitle(model, 'arviointi.-1.kuvaus')
    let pakollinen = modelData(model, 'koulutusmoduuli.pakollinen')
    if (pakollinen === false) {
      oppiaine = 'Valinnainen ' + oppiaine.toLowerCase() // i18n
    }
    let toggleExpand = () => { if (sanallinenArviointi) this.setState({expanded : !expanded}) }
    return (<tbody className={expanded && 'expanded'}>
      <tr>
        <td className="oppiaine">
          { showExpand && <a className={ sanallinenArviointi ? 'toggle-expand' : 'toggle-expand disabled'} onClick={toggleExpand}>{ expanded ? '' : ''}</a> }
          {
            showExpand && sanallinenArviointi ? <a className="nimi" onClick={toggleExpand}>{oppiaine}</a> : <span className="nimi">{oppiaine}</span>
          }
        </td>
        <td className="arvosana">
          <Editor model={ modelLookup(model, 'arviointi.-1.arvosana')}/>
          {modelData(model, 'yksilöllistettyOppimäärä') ? <sup className="yksilollistetty" title="Yksilöllistetty oppimäärä"> *</sup> : null}
          {modelData(model, 'painotettuOpetus') ? <sup className="painotettu" title="Painotettu opetus"> **</sup> : null}
          {modelData(model, 'korotus') ? <sup className="korotus" title="Perusopetuksen päättötodistuksen arvosanan korotus"> †</sup> : null}
        </td>
        {
          showLaajuus && (<td className="laajuus">
            <LaajuusEditor model={modelLookup(model, 'koulutusmoduuli.laajuus')} />
          </td>)
        }
      </tr>
      {
        !!sanallinenArviointi && expanded && <tr><td className="details" colSpan="2"><span className="sanallinen-arviointi">{sanallinenArviointi}</span></td></tr>
      }
    </tbody>)
  },
  getInitialState() {
    return { expanded: false }
  }
})

export const editorMapping = {
  'erityisentuenpaatos': JaksoEditor
}