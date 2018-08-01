import React from 'react'
import * as R from 'ramda'
import {LukionOppiaineEditor} from './LukionOppiaineEditor'
import {UusiLukionOppiaineDropdown} from './UusiLukionOppiaineDropdown'
import {modelErrorMessages, modelItems, modelLookup} from '../editor/EditorModel'
import {LukionOppiaineetTableHead} from './fragments/LukionOppiaineetTableHead'
import {t} from '../i18n/i18n'
import {flatMapArray} from '../util/util'
import {hyväksytystiSuoritetutKurssit, laajuudet} from './lukio'
import {laajuusNumberToString} from '../util/format.js'
import {isPaikallinen} from '../suoritus/Koulutusmoduuli'
import {FootnoteDescriptions} from '../components/footnote'

export const LukionOppiaineetEditor = ({suorituksetModel, classesForUusiOppiaineenSuoritus, suoritusFilter, additionalEditableKoulutusmoduuliProperties}) => {
  const {edit, suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel).filter(suoritusFilter || R.identity)

  if (!edit && R.isEmpty(oppiaineet)) return null

  const oppiaineRows = oppiaineet.map((oppiaine, oppiaineIndex) => (
    <LukionOppiaineEditor
      key={oppiaineIndex}
      oppiaine={oppiaine}
      additionalEditableKoulutusmoduuliProperties={additionalEditableKoulutusmoduuliProperties}
    />
  ))
  const errorRows = oppiaineet.map(oppiaine =>
    modelErrorMessages(oppiaine).map((error, i) =>
      <tr key={'error-' + i} className='error'><td colSpan='42' className='error'>{error}</td></tr>
    )
  )
  const oppiaineetWithErrorRows = R.zip(oppiaineRows, errorRows)

  return (
    <section>
      <table className="suoritukset oppiaineet">
        {!R.isEmpty(oppiaineet) && <LukionOppiaineetTableHead />}
        <tbody>
        {oppiaineetWithErrorRows}
        </tbody>
      </table>
      <div className="kurssit-yhteensä">{t('Suoritettujen kurssien laajuus yhteensä') + ': ' + laajuusNumberToString(laajuudet(arvioidutKurssit(oppiaineet)))}</div>
      {paikallisiaLukionOppiaineitaTaiKursseja(oppiaineet) && <FootnoteDescriptions data={[{title: 'Paikallinen kurssi tai oppiaine', hint: '*'}]}/>}
      <UusiLukionOppiaineDropdown
        model={päätasonSuoritusModel}
        oppiaineenSuoritusClasses={classesForUusiOppiaineenSuoritus}
      />
    </section>
  )
}

export const paikallisiaLukionOppiaineitaTaiKursseja = oppiaineet => oppiaineet.some(aine => isPaikallinen(modelLookup(aine, 'koulutusmoduuli')) || paikallisiaKursseja(aine))
export const paikallisiaKursseja = oppiaine => modelItems(oppiaine, 'osasuoritukset').some(kurssi => isPaikallinen(modelLookup(kurssi, 'koulutusmoduuli')))

export const arvioidutKurssit = oppiaineet => flatMapArray(oppiaineet, oppiaine => hyväksytystiSuoritetutKurssit(modelItems(oppiaine, 'osasuoritukset')))
