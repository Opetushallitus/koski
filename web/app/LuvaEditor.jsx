import React from 'react'
import * as Suoritustaulukko from './Suoritustaulukko.jsx'

export const LuvaEditor = React.createClass({
  render() {
    let {suoritukset} = this.props
    let lukionkurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukionkurssinsuoritus'))
    let lukioonvalmistavankurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukioonvalmistavankurssinsuoritus'))
    return (
      <div>
        {
          lukioonvalmistavankurssinsuoritukset.length > 0 &&
          <div>
            <h5>Lukioon valmistavat opinnot</h5>
            <Suoritustaulukko.SuorituksetEditor suoritukset={lukioonvalmistavankurssinsuoritukset}/>
          </div>
        }
        {
          lukionkurssinsuoritukset.length > 0 &&
          <div>
            <h5>Valinnaisena suoritetut lukiokurssit</h5>
            <Suoritustaulukko.SuorituksetEditor suoritukset={lukionkurssinsuoritukset}/>
          </div>
        }
      </div>
    )
  }
})