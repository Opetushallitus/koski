import React from 'react'

import {t} from '../i18n/i18n'
import {modelData, modelItems} from '../editor/EditorModel.js'
import {suorituksenTilaSymbol} from '../suoritus/Suoritustaulukko'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {tilaText} from '../suoritus/Suoritus'
import {FootnoteHint} from '../components/footnote'
import {pushRemoval} from '../editor/EditorModel'

export const LukionOppiaineEditor = ({oppiaine, footnote}) => {
  const arviointi = modelData(oppiaine, 'arviointi')
  const kurssit = modelItems(oppiaine, 'osasuoritukset')
  const suoritetutKurssit = kurssit.map(k => modelData(k)).filter(k => k.arviointi)
  const numeerinenArvosana = kurssi => parseInt(kurssi.arviointi.last().arvosana.koodiarvo)
  const kurssitNumeerisellaArvosanalla = suoritetutKurssit.filter(kurssi => !isNaN(numeerinenArvosana(kurssi)))
  const keskiarvo = kurssitNumeerisellaArvosanalla.length > 0 && Math.round((kurssitNumeerisellaArvosanalla.map(numeerinenArvosana).reduce((a, b) => a + b) / kurssitNumeerisellaArvosanalla.length) * 10) / 10
  const oppiaineTitle = t(modelData(oppiaine, 'koulutusmoduuli.tunniste.nimi'))
  const {edit} = oppiaine.context

  return (
    <tr className={'oppiaine oppiaine-rivi ' + modelData(oppiaine, 'koulutusmoduuli.tunniste.koodiarvo')}>
      <td className='suorituksentila' title={tilaText(oppiaine)}>
        <div>
          {suorituksenTilaSymbol(oppiaine)}
        </div>
      </td>
      <td className='oppiaine'>
        <div className='nimi'>{oppiaineTitle}</div>
        <KurssitEditor model={oppiaine}/>
      </td>
      <td className='maara'>{suoritetutKurssit.length}</td>
      <td className='arvosana'>
        <div className='annettuArvosana'>
          {arviointi ? modelData(oppiaine, 'arviointi.-1.arvosana').koodiarvo : '-'}
          {arviointi && footnote && <FootnoteHint title={footnote.title} hint={footnote.hint} />}
        </div>
        <div className='keskiarvo'>{keskiarvo ? '(' + keskiarvo.toFixed(1).replace('.', ',') + ')' : ''}</div>
      </td>
      {
        edit && (
          <td className='remove-row'>
            <a className='remove-value' onClick={() => pushRemoval(oppiaine)}/>
          </td>
        )
      }
    </tr>
  )
}
