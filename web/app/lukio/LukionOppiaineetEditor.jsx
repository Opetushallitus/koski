import React from 'react'
import * as R from 'ramda'
import {LukionOppiaineEditor} from './LukionOppiaineEditor'
import {UusiLukionOppiaineDropdown} from './UusiLukionOppiaineDropdown'
import {modelData, modelErrorMessages, modelItems, modelLookup} from '../editor/EditorModel'
import {LukionOppiaineetTableHead} from './fragments/LukionOppiaineetTableHead'
import {t} from '../i18n/i18n'
import {flatMapArray} from '../util/util'
import {hyväksytystiSuoritetutKurssit, laajuudet} from './lukio'
import {numberToString} from '../util/format.js'
import {isPaikallinen} from '../suoritus/Koulutusmoduuli'
import {FootnoteDescriptions} from '../components/footnote'
import {koodistoValues} from '../uusioppija/koodisto'

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
        {!R.isEmpty(oppiaineet) && <LukionOppiaineetTableHead laajuusyksikkö={laajuusYksikköP(oppiaineet[0])} />}
        <tbody>
        {oppiaineetWithErrorRows}
        </tbody>
      </table>
      <div className="kurssit-yhteensä">{t('Suoritettujen kurssien laajuus yhteensä') + ': ' + numberToString(laajuudet(arvioidutKurssit(oppiaineet)))}</div>
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

export const laajuusYksikköP = suoritus => {
  const laajuusYksikkö = modelData(suoritus, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo')
  return koodistoValues('opintojenlaajuusyksikko').map(yksiköt => yksiköt.find(y => y.koodiarvo === laajuusYksikkö)).map(y => y && y.nimi && y.nimi.fi)
}
