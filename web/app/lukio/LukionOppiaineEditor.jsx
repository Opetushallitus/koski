import React from 'react'

import {modelData, modelItems, modelTitle} from '../editor/EditorModel.js'
import {suorituksenTilaSymbol} from '../suoritus/Suoritustaulukko'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {tilaText} from '../suoritus/Suoritus'
import {FootnoteHint} from '../components/footnote'

export class LukionOppiaineEditor extends React.Component {
  render() {

    let {oppiaine, footnote} = this.props
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
          <div className="annettuArvosana">
            {arviointi ? modelData(oppiaine, 'arviointi.-1.arvosana').koodiarvo : '-'}
            {arviointi && footnote && <FootnoteHint title={footnote.title} hint={footnote.hint} />}
          </div>
          <div className="keskiarvo">{keskiarvo ? '(' + keskiarvo.toFixed(1).replace('.', ',') + ')' : ''}</div>
        </td>
      </tr>
    )
  }
}
