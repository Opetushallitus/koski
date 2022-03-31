import React from 'react'
import {t} from '../i18n/i18n'

export const TiedotPalvelussa = () => (
  <div className='tiedot-palvelussa'>
    <p>{t('Koski-palvelussa pystytään näyttämään seuraavat tiedot:')}</p>
    <ul>
      <li>{t('Vuoden 2018 tammikuun jälkeen suoritetut peruskoulun, lukion ja ammattikoulun opinnot ja voimassa olevat opiskeluoikeudet.')}</li>
      <li>{t('Vuoden 1990 jälkeen suoritetut ylioppilastutkinnot.')}</li>
      <li>{t('Korkeakoulutusuoritukset ja opiskeluoikeudet ovat näkyvissä pääsääntöisesti vuodesta 1995 eteenpäin, mutta tässä voi olla korkeakoulukohtaisia poikkeuksia.')}</li>
    </ul>
  </div>
)

TiedotPalvelussa.displayname = 'TiedotPalvelussa'
