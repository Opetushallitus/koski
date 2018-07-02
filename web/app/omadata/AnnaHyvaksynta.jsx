import React from 'baret'
import Text from '../i18n/Text'
import '../style/main.less'

export default ({ memberP, onAcceptClick, onCancelClick }) => (
  <div className="acceptance-box">
    <div className="acceptance-title"><Text name="Omadata hyväksyntä otsikko"/></div>
    <div className="acceptance-member-name">{memberP.map(member => member.name)}</div>
    <div className="acceptance-share-info">
      <Text name="Palveluntarjoaja näkee"/>
      <ul>
        <li><Text name="Näkee läsnäolotiedot"/></li>
        <li><Text name="Näkee oppilaitoksen tiedot"/></li>
      </ul>
    </div>
    <div className="acceptance-button-container">
      <div className="acceptance-button button" tabIndex={1} onClick={onAcceptClick}><Text name="Hyväksy"/></div>
      <span className="decline-link" onClick={onCancelClick} tabIndex={1}><Text name="Peruuta ja palaa"/></span>
    </div>
  </div>
)
