import './polyfills/polyfills.js'
import React from 'react'
import Text from './i18n/Text'
import ReactDOM from 'react-dom'
import './style/main.less'

const LanderInfo = () => (
  <div>
    <div className="koski-heading"><h1>{'KOSKI'}</h1></div>
    <div className="lander">
      <button className="login-button" onClick={() => window.location=window.kansalaisenAuthUrl}><Text name="Kirjaudu sisään" /></button>
      <div className="lander__caption">
        <div className="lander__column lander__left">
          <p><Text name="Lander ingressi 1"/></p>
          <p><Text name="Lander ingressi 2"/></p>
        </div>
        <div className="lander__column lander__right">
          <p><Text name="Lander ingressi 3"/></p>
          <p><Text name="Lander ingressi 4"/></p>
        </div>
      </div>
      <p className="tietosuojaseloste"><a href="https://confluence.csc.fi/download/attachments/58828884/Tietosuojaseloste%20KOSKI%209.2.2018.pdf?api=v2"><Text name="KOSKI-palvelun tietosuojaseloste (sisältää rekisteriselosteen)"/></a></p>
    </div>
    <div className="lander-logo">
      <img src="/koski/images/oph_fin_vaaka.png" />
      <img src="/koski/images/logo_okm.png" />
    </div>
  </div>
)

ReactDOM.render((
  <div>
    <LanderInfo/>
  </div>
), document.getElementById('content'))

document.body.id = 'lander-page'
