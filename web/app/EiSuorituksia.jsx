import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import { EiSuorituksiaInfo } from './omattiedot/EiSuorituksiaInfo'
import { t } from './i18n/i18n'

function EiSuorituksiaContainer() {
  return (
    <div>
      <h1>{t('Opintoni')}</h1>
      <EiSuorituksiaInfo />
    </div>
  )
}

ReactDOM.render(<EiSuorituksiaContainer />, document.getElementById('content'))
