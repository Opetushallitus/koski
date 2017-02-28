import React from 'react'
import * as Suoritustaulukko from './Suoritustaulukko.jsx'

export const LuvaEditor = React.createClass({
  render() {
    let {suoritukset, context} = this.props
    let lukionkurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukionkurssinsuoritus'))
    let lukioonvalmistavankurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukioonvalmistavankurssinsuoritus'))
    return (
      <div>
        {
          lukioonvalmistavankurssinsuoritukset.length > 0 &&
          <div>
            <h5>Lukioon valmistavat opinnot</h5>
            <Suoritustaulukko.SuorituksetEditor context={context} suoritukset={lukioonvalmistavankurssinsuoritukset}/>
          </div>
        }
        {
          lukionkurssinsuoritukset.length > 0 &&
          <div>
            <h5>Valinnaisena suoritetut lukiokurssit</h5>
            <Suoritustaulukko.SuorituksetEditor context={context} suoritukset={lukionkurssinsuoritukset}/>
          </div>
        }
      </div>
    )
  }
})