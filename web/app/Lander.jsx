import './polyfills/polyfills.js'
import React from 'react'
import Text from './i18n/Text'
import ReactDOM from 'react-dom'
import './style/main.less'
import {TopBar} from './topbar/TopBar'

const LanderInfo = () => (
  <div className={'lander'}>
    <h1><Text name="Koski-palvelu"/></h1>
    <p><Text name="Koulutustiedot luotettavasti yhdestä paikasta"/></p>
    <div className={'lander__caption'}>
      <p><Text name="Lander ingressi 1"/></p>
      <p><Text name="Lander ingressi 2"/></p>
      <p><Text name="Virkailijapalvelut löydät jatkossa osoitteesta"/> <a href="/koski/virkailija">{window.location.origin + '/koski/virkailija'}</a></p>
      <p><button onClick={() => window.location=window.kansalaisenAuthUrl}><Text name="Kirjaudu sisään" /></button></p>
    </div>
  </div>
)

ReactDOM.render((
  <div>
    <TopBar user={null} />
    <LanderInfo/>
  </div>
), document.getElementById('content'))

document.body.id = 'lander-page'