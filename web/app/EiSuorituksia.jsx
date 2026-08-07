// eslint-disable-next-line no-undef
import React from 'react'
import ReactDOM from 'react-dom'
import { EiSuorituksiaInfo } from './omattiedot/EiSuorituksiaInfo'
import { t } from './i18n/i18n'
import { loadStyles } from './util/loadStyles'

__webpack_nonce__ = window.nonce
loadStyles(() => import(/* webpackChunkName: "styles" */ './style/main.less'))

ReactDOM.render(
  <div>
    <h1>{t('Opintoni')}</h1>
    <EiSuorituksiaInfo />
  </div>,
  document.getElementById('content')
)
