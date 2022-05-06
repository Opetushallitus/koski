import './style/main.less'
import React from 'react'
import ReactDOM from 'react-dom'
import {EiSuorituksiaInfo} from './omattiedot/EiSuorituksiaInfo'
import {t} from './i18n/i18n'

ReactDOM.render((
  <div>
    <h1>{t('Opintoni')}</h1>
    <EiSuorituksiaInfo/>
  </div>
), document.getElementById('content'))

