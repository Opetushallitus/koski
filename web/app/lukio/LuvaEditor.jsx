import React from 'react'
import {LukionOppiaineetEditor} from './LukionOppiaineetEditor'
import Text from '../i18n/Text'

export class LuvaEditor extends React.Component {
  render() {
    let {suoritukset} = this.props
    let lukionkurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa'))
    let lukioonvalmistavankurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukioonvalmistavankoulutuksenoppiaineensuoritus'))
    return (
      <div>
        {
          lukioonvalmistavankurssinsuoritukset.length > 0 &&
          <div>
            <h5><Text name="Lukioon valmistavat opinnot"/></h5>
            <LukionOppiaineetEditor oppiaineet={lukioonvalmistavankurssinsuoritukset} />
          </div>
        }
        {
          lukionkurssinsuoritukset.length > 0 &&
          <div>
            <h5><Text name="Valinnaisena suoritetut lukiokurssit"/></h5>
            <LukionOppiaineetEditor oppiaineet={lukionkurssinsuoritukset} />
          </div>
        }
      </div>
    )
  }
}
