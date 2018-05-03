import React from 'react'
import {TiedotPalvelussa} from './TiedotPalvelussa'
import {t} from '../i18n/i18n'

export const EiSuorituksiaInfo = () => (
  <div className='ei-suorituksia'>
    <h2>{t('Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia.')}</h2>
    <TiedotPalvelussa/>
    <p>{t('Mikäli tiedoistasi puuttuu opintosuorituksia tai opiskeluoikeuksia, joiden kuuluisi ylläolevien tietojen perusteella näkyä Koski-palvelussa, voit ilmoittaa asiasta oppilaitoksellesi.')}</p>
  </div>
)
