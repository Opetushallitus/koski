import React from 'baret'
import Text from '../i18n/Text'
import '../style/main.less'

export default ({ memberName, memberPurpose, onAcceptClick, logoutURL }) => (
  <div className='acceptance-box'>
    <div className='acceptance-title'><Text name='Hyväksymällä annat tälle palveluntarjoajalle luvan käyttää opintotietojasi.'/></div>
    <div className='acceptance-member-name'>{memberName}</div>
    <div className='acceptance-share-info'>
      <Text name='Palveluntarjoaja saa seuraavat tiedot'/>
      <ul>
        <li><Text name='Tiedot opiskeluoikeuksistasi'/></li>
        <li><Text name='Nimesi ja syntymäaikasi'/></li>
      </ul>
      <div className='acceptance-member-purpose'>{memberPurpose}</div>
    </div>
    <div className='acceptance-duration'><Text name='Lupa on voimassa vuoden. Voit perua luvan Oma Opintopolku-palvelussa.'/></div>
    <div className='acceptance-button-container'>
      <button className='acceptance-button koski-button' onClick={onAcceptClick}><Text name='Hyväksy'/></button>
      <div className='decline-link'> <a href={logoutURL}><Text name='Peruuta ja palaa'/></a> </div>
    </div>
  </div>
)
