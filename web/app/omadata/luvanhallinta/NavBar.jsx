import React from 'react'
//import constants from 'ui/constants'
//import media from 'ui/media'
import Text from '../../i18n/Text'
import {t} from '../../i18n/i18n'

const NavBar = () => (
  <div className='navigation'>
    <a className='link' href='/koski/omadata/kayttooikeudet'>
      <span className='inactiveTab'><Text name={'Annetut käyttöluvat'} /></span>
    </a>
    <span className='activeTab'><Text name={'Tietojani käyttäneet toimijat'} /></span>
    <span className='filler'/>
  </div>
)

export default NavBar
