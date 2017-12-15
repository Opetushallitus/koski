import React from 'react'
import {modelData, modelItems, modelTitle} from '../editor/EditorModel.js'
import {suorituksenTilaSymbol} from '../suoritus/Suoritustaulukko'
import Text from '../i18n/Text'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {tilaText} from '../suoritus/Suoritus'

export class LukionOppiaineetEditor extends React.Component {
  render() {
    let {oppiaineet} = this.props
    return (
      <table className="suoritukset oppiaineet">
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
}

class LukionOppiaineEditor extends React.Component {
  render() {

    let {oppiaine} = this.props
    let arviointi = modelData(oppiaine, 'arviointi')
    let kurssit = modelItems(oppiaine, 'osasuoritukset')
    let suoritetutKurssit = kurssit.map(k => modelData(k)).filter(k => k.arviointi)
    let numeerinenArvosana = kurssi => parseInt(kurssi.arviointi.last().arvosana.koodiarvo)
    let kurssitNumeerisellaArvosanalla = suoritetutKurssit.filter(kurssi => !isNaN(numeerinenArvosana(kurssi)))
    let keskiarvo = kurssitNumeerisellaArvosanalla.length > 0 && Math.round((kurssitNumeerisellaArvosanalla.map(numeerinenArvosana).reduce((a, b) => a + b) / kurssitNumeerisellaArvosanalla.length) * 10) / 10

    return (
      <tr className={'oppiaine oppiaine-rivi ' + modelData(oppiaine, 'koulutusmoduuli.tunniste.koodiarvo')}>
        <td className="suorituksentila" title={tilaText(oppiaine)}>
          <div>
            {suorituksenTilaSymbol(oppiaine)}
          </div>
        </td>
        <td className="oppiaine">
          <div className="nimi">{modelTitle(oppiaine, 'koulutusmoduuli')}</div>
          <KurssitEditor model={oppiaine}/>
        </td>
        <td className="maara">{suoritetutKurssit.length}</td>
        <td className="arvosana">
          <div
            className="annettuArvosana">{arviointi ? modelData(oppiaine, 'arviointi.-1.arvosana').koodiarvo : '-'}</div>
          <div className="keskiarvo">{keskiarvo ? '(' + keskiarvo.toFixed(1).replace('.', ',') + ')' : ''}</div>
        </td>
      </tr>
    )
  }
}