import React from 'react'
import * as R from 'ramda'
import {LukionOppiaineEditor} from './LukionOppiaineEditor'
import {UusiLukionOppiaineDropdown} from './UusiLukionOppiaineDropdown'
import {modelErrorMessages, modelItems, modelLookup, modelTitle} from '../editor/EditorModel'
import {LukionOppiaineetTableHead} from './fragments/LukionOppiaineetTableHead'
import {t} from '../i18n/i18n'
import {flatMapArray} from '../util/util'
import {hyväksytystiSuoritetutOsasuoritukset, isLukioOps2019, laajuudet} from './lukio'
import {numberToString} from '../util/format.js'
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
      customOsasuoritusTitle='osasuoritus'
      showArviointiEditor={!oppiaine.value.classes.includes('arvioinniton')}
    />
  ))
  const errorRows = oppiaineet.map(oppiaine =>
    modelErrorMessages(oppiaine).map((error, i) =>
      <tr key={'error-' + i} className='error'><td colSpan='42' className='error'>{error}</td></tr>
    )
  )
  const oppiaineetWithErrorRows = R.zip(oppiaineRows, errorRows)
  const laajuusyksikkö = modelTitle(oppiaineet[0], 'koulutusmoduuli.laajuus.yksikkö') || 'kurssia'
  return (
    <section>
      <table className="suoritukset oppiaineet">
        {!R.isEmpty(oppiaineet) && <LukionOppiaineetTableHead laajuusyksikkö={laajuusyksikkö} />}
        <tbody>
        {oppiaineetWithErrorRows}
        </tbody>
      </table>
      <div className="kurssit-yhteensä">{t(osasuoritustenLaajuusYhteensäText(suorituksetModel.context.suoritus)) + ': ' + numberToString(laajuudet(arvioidutOsasuoritukset(oppiaineet)))}</div>
      {paikallisiaLukionOppiaineitaTaiOsasuorituksia(oppiaineet) && <FootnoteDescriptions data={[{title: paikallinenOsasuoritusTaiOppiaineText(suorituksetModel.context.suoritus), hint: '*'}]}/>}
      <UusiLukionOppiaineDropdown
        model={päätasonSuoritusModel}
        oppiaineenSuoritusClasses={classesForUusiOppiaineenSuoritus}
      />
    </section>
  )
}

export const paikallisiaLukionOppiaineitaTaiOsasuorituksia = oppiaineet => oppiaineet.some(aine => isPaikallinen(modelLookup(aine, 'koulutusmoduuli')) || paikallisiaOsasuorituksia(aine))
export const paikallisiaOsasuorituksia = oppiaine => modelItems(oppiaine, 'osasuoritukset').some(osasuoritus => isPaikallinen(modelLookup(osasuoritus, 'koulutusmoduuli')))

export const arvioidutOsasuoritukset = oppiaineet => flatMapArray(oppiaineet, oppiaine => hyväksytystiSuoritetutOsasuoritukset(modelItems(oppiaine, 'osasuoritukset')))

export const osasuoritustenLaajuusYhteensäText = päätasonSuoritus => isLukioOps2019(päätasonSuoritus) ?
  'Suoritettujen osasuoritusten laajuus yhteensä' : 'Suoritettujen kurssien laajuus yhteensä'

export const paikallinenOsasuoritusTaiOppiaineText = päätasonSuoritus => isLukioOps2019(päätasonSuoritus) ?
  'Paikallinen opintojakso tai oppiaine' : 'Paikallinen kurssi tai oppiaine'
