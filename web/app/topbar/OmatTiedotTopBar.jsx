import React from 'baret'
import {UserInfo} from './UserInfo'
import Link from '../components/Link'
import Text from '../i18n/Text'

const OmatTiedotTopBar = ({user}) => {
  return (
    <header id='topbar' className="local topbar-omattiedot">
      <div className='topbar-content-wrapper'>
        <div id='logo'><Text name="Opintopolku.fi"/></div>
        <h1>
          <Link href="/koski/"><Text name="Koski"/></Link>
          <span>{' - '}<Text name="Omat tiedot"/></span>
        </h1>
        <UserInfo user={user}/>
      </div>
    </header>
  )
}

OmatTiedotTopBar.displayName = 'OmatTiedotTopBar'

export default OmatTiedotTopBar
