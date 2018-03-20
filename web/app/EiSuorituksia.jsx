import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import {TiedotPalvelussa} from './omattiedot/TiedotPalvelussa'

export const EiSuorituksia = () => (
  <div className='ei-suorituksia'>
    <h2>{'Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia.'}</h2>
    <TiedotPalvelussa/>
    <p>{'Mikäli tiedoistasi puuttuu opintosuorituksia tai opiskeluoikeuksia, joiden kuuluisi ylläolevien tietojen perusteella näkyä Koski-palvelussa, voit ilmoittaa asiasta oppilaitoksellesi.'}</p>
  </div>
)

ReactDOM.render((
  <div>
    <div className='koski-heading'><h1>{'KOSKI'}</h1></div>
    <EiSuorituksia/>
  </div>
), document.getElementById('content'))

