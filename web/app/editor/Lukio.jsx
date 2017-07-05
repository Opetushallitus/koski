import React from 'react'
import {modelData, modelTitle, modelItems} from './EditorModel.js'
import {suorituksenTilaSymbol} from './Suoritustaulukko.jsx'
import {KurssiEditor} from './KurssiEditor.jsx'
import Text from '../Text.jsx'

export const LukionOppiaineetEditor = React.createClass({
  render() {
    let {oppiaineet} = this.props
    return (
      <table className="suoritukset">
        <thead>
        <tr>
          <th className="suorituksentila"></th>
          <th className="oppiaine"><Text name="Oppiaine"/></th>
          <th className="maara"><Text name="Kurssien määrä"/></th>
          <th className="arvosana"><Text name="Arvosana (keskiarvo)"/></th>
        </tr>
        <tr>
          <th colSpan="4"><hr/></th>
        </tr>
        </thead>
        <tbody>
        {
          oppiaineet.map((oppiaine, oppiaineIndex) =>
            <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} />
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
    let kurssit = modelItems(oppiaine, 'osasuoritukset')
    let suoritetutKurssit = kurssit.map(k => modelData(k)).filter(k => k.arviointi)
    let numeerinenArvosana = kurssi => parseInt(kurssi.arviointi.last().arvosana.koodiarvo)
    let kurssitNumeerisellaArvosanalla = suoritetutKurssit.filter(kurssi => !isNaN(numeerinenArvosana(kurssi)))
    let keskiarvo = kurssitNumeerisellaArvosanalla.length > 0 && Math.round((kurssitNumeerisellaArvosanalla.map(numeerinenArvosana).reduce((a,b) => a + b) / kurssitNumeerisellaArvosanalla.length) * 10) / 10

    return (
      <tr>
        <td className="suorituksentila" title={modelTitle(oppiaine, 'tila')}>
          <div>
          {suorituksenTilaSymbol(modelData(oppiaine, 'tila.koodiarvo'))}
          </div>
        </td>
        <td className="oppiaine">
          <div className="nimi">{modelTitle(oppiaine, 'koulutusmoduuli')}</div>
          <ul className="kurssit">
            {
              kurssit.map((kurssi, kurssiIndex) =>
                <KurssiEditor key={kurssiIndex} kurssi={kurssi}/>
              )
            }
          </ul>
        </td>
        <td className="maara">{suoritetutKurssit.length}</td>
        <td className="arvosana">
          <div className="annettuArvosana">{arviointi ? modelData(oppiaine, 'arviointi.-1.arvosana').koodiarvo : '-'}</div>
          <div className="keskiarvo">{keskiarvo ? '(' + keskiarvo.toFixed(1).replace('.', ',') + ')' : ''}</div>
        </td>
      </tr>
    )
  }
})