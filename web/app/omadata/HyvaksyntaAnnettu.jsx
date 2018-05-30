import React from 'react'
import Text from '../i18n/Text'
import '../style/main.less'
import {lang} from '../i18n/i18n'

export default ({ memberName, onAcceptClick }) => (
  <div className="acceptance-box">
    <div className="acceptance-title"><Text name="Omadata hyväksytty otsikko"/></div>
    <div className="acceptance-share-info">
      <Text name="Voit hallita tietojasi"/>
    </div>
    <div className="acceptance-button-container">
      <span className="decline-link"><Text name="Palataan palveluntarjoajan sivulle"/></span>
      <div className="acceptance-button button" onClick={onAcceptClick}><Text name="Palaa palveluntarjoajan sivulle"/></div>
    </div>
  </div>
)
