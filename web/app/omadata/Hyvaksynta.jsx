import '../polyfills/polyfills.js'
import React from 'react'
import Text from '../i18n/Text'
import ReactDOM from 'react-dom'
import '../style/main.less'
import {lang} from '../i18n/i18n'

const Hyvaksynta = () => (
  <div>
    <div className="koski-heading"><h1>{'Oma Opintopolku'}</h1></div>
    <div className="koski-mydata-body">

    </div>
    <div className="lander-logo">
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

