import React from 'react'
import R from 'ramda'
import {LukionOppiaineEditor} from './LukionOppiaineEditor'
import {LukionOppiaineetTableHead} from './fragments/LukionOppiaineetTable'
import {UusiLukionOppiaineDropdown} from './UusiLukionOppiaineDropdown'
import {modelErrorMessages} from '../editor/EditorModel'

export const LukionOppiaineetEditor = ({päätasonSuoritusModel, suoritukset}) => {
  const oppiaineet = suoritukset || modelItems(päätasonSuoritusModel, 'osasuoritukset') || []
  const oppiaineRows = oppiaineet.map((oppiaine, oppiaineIndex) =>
    <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} />
  )
  const errorRows = oppiaineet.map(oppiaine =>
    modelErrorMessages(oppiaine).map((error, i) =>
      <tr key={'error-' + i} className='error'><td colSpan='42' className='error'>{error}</td></tr>
    )
  )
  const oppiaineetWithErrorRows = R.zip(oppiaineRows, errorRows)

  return (
    <section>
      <table className="suoritukset oppiaineet">
        <LukionOppiaineetTableHead />
        <tbody>
        {oppiaineetWithErrorRows}
        </tbody>
      </table>
      <UusiLukionOppiaineDropdown model={päätasonSuoritusModel} />
    </section>
  )
}
