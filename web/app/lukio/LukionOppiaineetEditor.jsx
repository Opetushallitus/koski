import React from 'react'
import {LukionOppiaineEditor} from './LukionOppiaineEditor'
import {LukionOppiaineetTableHead} from './fragments/LukionOppiaineetTable'

export class LukionOppiaineetEditor extends React.Component {
  render() {
    let {oppiaineet} = this.props
    return (
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
  }
}
