import './polyfills/polyfills.js'
import React from 'react'
import Text from './i18n/Text'
import {t} from './i18n/i18n'
import ReactDOM from 'react-dom'
import './style/main.less'
import {lang} from './i18n/i18n'

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
      <p className="tietosuojaseloste"><TietosuojaselosteLinkki><Text name="KOSKI-palvelun tietosuojaseloste (sisältää rekisteriselosteen)"/></TietosuojaselosteLinkki></p>
      <button className='login-button' onClick={() => window.location=window.kansalaisenAuthUrl}><Text name="Kirjaudu sisään" /></button>
    </div>
  </div>
)

const TietosuojaselosteLinkki = ({children}) => {
  const linkki = (lang === 'sv')
    ? 'https://confluence.csc.fi/download/attachments/58828884/Dataskyddsbeskrivning%20KOSKI%2012.2.2018.pdf?api=v2'
    : 'https://confluence.csc.fi/download/attachments/64962405/Tietosuojaseloste%20-%20KOSKI-palvelu%2023.5.2018.pdf?api=v2'
  return <a href={linkki}>{children}</a>
}

ReactDOM.render((
  <div>
    <LanderInfo/>
  </div>
), document.getElementById('content'))

