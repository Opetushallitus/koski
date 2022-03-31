import React from 'react'
import Text from '../../i18n/Text'

const NavBar = () => (
  <div className='navigation'>
    <span className='activeTab'><Text name='Annetut käyttöluvat' /></span>
    <a className='link' href='/oma-opintopolku-loki/'>
      <span className='inactiveTab'><Text name='Tietojani käyttäneet toimijat' /></span>
    </a>
    <span className='filler'/>
  </div>
)

NavBar.displayName = 'NavBar'

export default NavBar
