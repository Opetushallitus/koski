// eslint-disable-next-line no-undef
import React from 'react'
import ReactDOM from 'react-dom'
import { EiSuorituksiaInfo } from './omattiedot/EiSuorituksiaInfo'
import { t } from './i18n/i18n'
__webpack_nonce__ = window.nonce
import(/* webpackChunkName: "styles" */ './style/main.less')

ReactDOM.render(
  <div>
    <h1>{t('Opintoni')}</h1>
    <EiSuorituksiaInfo />
  </div>,
  document.getElementById('content')
)
