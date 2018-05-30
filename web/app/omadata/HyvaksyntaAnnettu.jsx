import React from 'react'
import Text from '../i18n/Text'
import '../style/main.less'
import {lang} from '../i18n/i18n'

export default ({ memberName, onAcceptClick }) => (
  <div className="acceptance-box">
    <div className="acceptance-title-success"><Text name="Omadata hyväksytty otsikko"/></div>
    <div className="acceptance-control-mydata">
      <Text name="Voit hallita tietojasi"/>
    </div>
    <div className="acceptance-return-container">
      <div className="acceptance-return-automatically"><Text name="Palataan palveluntarjoajan sivulle"/></div>
      <div className="acceptance-return-button button" onClick={onAcceptClick}><Text name="Palaa palveluntarjoajan sivulle"/></div>
    </div>
  </div>
)
