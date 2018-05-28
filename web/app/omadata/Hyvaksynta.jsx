import '../polyfills/polyfills.js'
import React from 'react'
import Text from '../i18n/Text'
import ReactDOM from 'react-dom'
import '../style/main.less'
import {lang} from '../i18n/i18n'

const Hyvaksynta = () => (
  <div>
    <div className="header">
      <div className="title"><h1><Text name="Oma Opintopolku"/></h1></div>
      <div className="user">
        <div className="username">Clara Nieminen</div>
        <div className="logout"><Text name="Kirjaudu ulos"/></div>
      </div>
    </div>
    <div className="heading"><h1><Text name="Henkilökohtaisten tietojen käyttö"/></h1></div>
    <div className="user"></div>
    <div className="acceptance-box">
      <div className="acceptance-title"><Text name="Omadata hyväksyntä otsikko"/></div>
      <div className="acceptance-member-name"> </div>
      <div className="acceptance-share-info">
        <Text name="Palveluntarjoaja näkee"/>
        <Text name="Näkee läsnäolotiedot"/>
        <Text name="Näkee oppilaitoksen tiedot"/>
      </div>
      <div className="acceptance-button-container"> </div>
    </div>

    <div className="footer">
      <img src="/koski/images/oph_fin_vaaka.png" />
      <img src="/koski/images/logo_okm.png" />
    </div>
  </div>
)


ReactDOM.render((
  <div>
    <Hyvaksynta/>
  </div>
), document.getElementById('content'))

