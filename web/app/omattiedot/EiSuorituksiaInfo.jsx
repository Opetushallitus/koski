import React from 'react'
import {TiedotPalvelussa} from './TiedotPalvelussa'
import {t} from '../i18n/i18n'

export const EiSuorituksiaInfo = ({oppija}) => {
  const huollettava = oppija && oppija.context && oppija.context.huollettava
  return (
    <div className='ei-suorituksia'>
      <h2>{huollettava
        ? t('Huollettavasi tiedoilla ei löydy opintosuorituksia eikä opiskeluoikeuksia.')
        : t('Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia.')
      }</h2>
      <TiedotPalvelussa/>
      <p>{huollettava
        ? t('Mikäli tiedoistasi puuttuu opintosuorituksia tai opiskeluoikeuksia, joiden kuuluisi ylläolevien tietojen perusteella näkyä Koski-palvelussa, voit ilmoittaa asiasta oppilaitoksellesi.')
        : t('Mikäli huollettavasi tiedoista puuttuu opintosuorituksia tai opiskeluoikeuksia, joiden kuuluisi ylläolevien tietojen perusteella näkyä Koski-palvelussa, voit ilmoittaa asiasta oppilaitoksellesi.')
    }</p>
    </div>
  )
}

EiSuorituksiaInfo.displayName = 'EiSuorituksiaInfo'
