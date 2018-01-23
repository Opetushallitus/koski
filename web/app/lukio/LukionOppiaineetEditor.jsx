import React from 'react'
import {LukionOppiaineEditor} from './LukionOppiaineEditor'
import {LukionOppiaineetTableHead} from './fragments/LukionOppiaineetTable'

export const LukionOppiaineetEditor = ({oppiaineet}) => (
  <table className="suoritukset oppiaineet">
    <LukionOppiaineetTableHead />
    <tbody>
    {
      oppiaineet.map((oppiaine, oppiaineIndex) =>
        <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} />
      )
    }
    </tbody>
  </table>
)
