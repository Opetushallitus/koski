import React from 'react'
import Text from '../i18n/Text'
import '../style/main.less'
import {lang} from '../i18n/i18n'

export default ({ memberName, onAcceptClick }) => (
  <div className="acceptance-box">
    <div className="acceptance-title"><Text name="Omadata hyväksyntä otsikko"/></div>
    <div className="acceptance-member-name">{memberName}</div>
    <div className="acceptance-share-info">
      <Text name="Palveluntarjoaja näkee"/>
      <ul>
        <li><Text name="Näkee läsnäolotiedot"/></li>
        <li><Text name="Näkee oppilaitoksen tiedot"/></li>
      </ul>
    </div>
    <div className="acceptance-button-container">
      <div className="acceptance-button button" onClick={onAcceptClick}><Text name="Hyväksy"/></div>
      <span className="decline-link"><Text name="Peruuta ja palaa"/></span>
    </div>
  </div>
)
