import './style/main.less'
import './polyfills/polyfills.js'
import React from 'react'
import Text from './i18n/Text'
import {t} from './i18n/i18n'
import {patchSaavutettavuusLeima} from './saavutettavuusLeima'
import ReactDOM from 'react-dom'

const LanderInfo = () => (
  <div>
    <div className="lander">
      <h1>{t('Opintoni')}</h1>
      <div>
        <p className='textstyle-lead'><Text name='Lander ingressi 1'/></p>
        <p><Text name='Lander ingressi 2'/></p>
        <p><Text name='Lander ingressi 3'/></p>
        <p><Text name='Lander ingressi 4'/></p>
      </div>
      <button className='koski-button login-button' onClick={() => window.location=window.kansalaisenAuthUrl}><Text name="Kirjaudu sisään" /></button>
    </div>
  </div>
)

ReactDOM.render((
  <div>
    <LanderInfo/>
  </div>
), document.getElementById('content'))

patchSaavutettavuusLeima()
