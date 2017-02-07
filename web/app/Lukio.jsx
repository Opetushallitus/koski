import React from 'react'
import { modelData, modelLookup, modelTitle, modelItems } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'

export const LukionOppiaineetEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let oppiaineet = modelItems(model, 'osasuoritukset') || []
    return (
      <table className="suoritukset">
        <thead>
        <tr>
          <th className="oppiaine">Oppiaine</th>
          <th className="maara">Kurssien määrä</th>
          <th className="arvosana">Arvosana (keskiarvo)</th>
        </tr>
        <tr>
          <th colSpan="3"><hr/></th>
        </tr>
        </thead>
        <tbody>
        {
          oppiaineet.map((oppiaine, oppiaineIndex) =>
            <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} context={GenericEditor.childContext(this, context, 'osasuoritukset')}/>
          )
        }
        </tbody>
      </table>
    )
  }
})

const LukionOppiaineEditor = React.createClass({
  render() {
    let {oppiaine} = this.props
    let arviointi = modelData(oppiaine, 'arviointi')
    let kurssit = modelItems(oppiaine, 'osasuoritukset') || []
    let suoritetutKurssit = modelData(oppiaine, 'osasuoritukset').filter(k => k.arviointi)
    let kurssiLkm = suoritetutKurssit ? suoritetutKurssit.length : 0
    //let keskiarvo = kurssiLkm > 0 && Math.round((suoritetutKurssit.map(k => parseInt(k.arviointi.last().arvosana.koodiarvo)).reduce((a,b) => a + b) / kurssiLkm) * 10) / 10
    return (
      <tr>
        <td className="oppiaine">
          <div className="nimi">{modelTitle(oppiaine, 'koulutusmoduuli')}</div>
          <ul className="kurssit">
            {
              kurssit.map((kurssi, kurssiIndex) =>
                <LukionKurssiEditor key={kurssiIndex} kurssi={kurssi}/>
              )
            }
          </ul>
        </td>
        <td className="maara">{kurssiLkm}</td>
        <td className="arvosana">
          <div className="annettuArvosana">{arviointi ? modelData(oppiaine, 'arviointi.-1.arvosana').koodiarvo : '-'}</div>
          <div className="keskiarvo"></div>
        </td>
      </tr>
    )
  }
})

const LukionKurssiEditor = React.createClass({
  render() {
    let {kurssi} = this.props
    let arviointi = modelData(kurssi, 'arviointi')
    return (
      <li className="kurssi">
        <div className="tunniste">{modelData(kurssi, 'koulutusmoduuli.tunniste.koodiarvo')}</div>
        <div
          className="arvosana">{arviointi && modelData(kurssi, 'arviointi.-1.arvosana').koodiarvo}</div>
      </li>
    )
  }
})