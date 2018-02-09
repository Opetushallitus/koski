import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'

export const EiSuorituksia = () => (
  <div className='ei-suorituksia'>
    <div className='koski-heading'><h1>{'KOSKI'}</h1></div>
    <div className='info'>
      <h2>{'Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia.'}</h2>
      <p>{'Koski-palvelussa pystytään näyttämään seuraavat tiedot:'}</p>
      <ul>
        <li>{'Vuoden 2018 tammikuun jälkeen suoritetut peruskoulun, lukion ja ammattikoulun opinnot ja voimassa olevat opiskeluoikeudet.'}</li>
        <li>{'Vuoden 1990 jälkeen suoritetut ylioppilastutkinnot.'}</li>
        <li>{'Korkeakoulutusuoritukset ja opiskeluoikeudet ovat näkyvissä pääsääntöisesti vuodesta 1995 eteenpäin, mutta tässä voi olla korkeakoulukohtaisia poikkeuksia.'}</li>
      </ul>
      <p>{'Mikäli tiedoistasi puuttuu opintosuorituksia tai opiskeluoikeuksia, joiden kuuluisi ylläolevien tietojen perusteella näkyä Koski-palvelussa, voit ilmoittaa asiasta oppilaitoksellesi.'}</p>
    </div>
  </div>
)

ReactDOM.render((
  <div>
    <EiSuorituksia/>
  </div>
), document.getElementById('content'))

document.body.id = 'lander-page'
