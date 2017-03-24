import React from 'react'
import { LukionOppiaineetEditor } from './Lukio.jsx'

export const LuvaEditor = React.createClass({
  render() {
    let {suoritukset} = this.props
    let lukionkurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa'))
    let lukioonvalmistavankurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukioonvalmistavankoulutuksenoppiaineensuoritus'))
    return (
      <div>
        {
          lukioonvalmistavankurssinsuoritukset.length > 0 &&
          <div>
            <h5>Lukioon valmistavat opinnot</h5>
            <LukionOppiaineetEditor oppiaineet={lukioonvalmistavankurssinsuoritukset} />
          </div>
        }
        {
          lukionkurssinsuoritukset.length > 0 &&
          <div>
            <h5>Valinnaisena suoritetut lukiokurssit</h5>
            <LukionOppiaineetEditor oppiaineet={lukionkurssinsuoritukset} />
          </div>
        }
      </div>
    )
  }
})