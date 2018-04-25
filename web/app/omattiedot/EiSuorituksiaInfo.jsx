import React from 'react'
import {TiedotPalvelussa} from './TiedotPalvelussa'

export const EiSuorituksiaInfo = () => (
  <div className='ei-suorituksia'>
    <h2>{'Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia.'}</h2>
    <TiedotPalvelussa/>
    <p>{'Mikäli tiedoistasi puuttuu opintosuorituksia tai opiskeluoikeuksia, joiden kuuluisi ylläolevien tietojen perusteella näkyä Koski-palvelussa, voit ilmoittaa asiasta oppilaitoksellesi.'}</p>
  </div>
)
