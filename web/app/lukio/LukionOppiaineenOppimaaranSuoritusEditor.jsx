import React from 'react'
import {modelErrorMessages} from '../editor/EditorModel'
import {LukionOppiaineEditor} from './LukionOppiaineEditor'
import {LukionOppiaineetTableHead} from './fragments/LukionOppiaineetTableHead'

export const LukionOppiaineenOppimaaranSuoritusEditor = ({model}) => (
  <section>
    <table className="suoritukset oppiaineet">
      <LukionOppiaineetTableHead />
      <tbody>
      <LukionOppiaineEditor oppiaine={model} allowOppiaineRemoval={false} />
      {
        modelErrorMessages(model).map((error, i) =>
          <tr key={'error-' + i} className='error'><td colSpan='42' className='error'>{error}</td></tr>
        )
      }
      </tbody>
    </table>
  </section>
)
