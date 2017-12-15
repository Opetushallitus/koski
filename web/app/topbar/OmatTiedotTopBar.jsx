import React from 'baret'
import {UserInfo} from './UserInfo.jsx'
import Link from '../Link.jsx'
import Text from '../Text.jsx'

export default ({user}) => {
  return (
    <header id='topbar' className="local">
      <div id='logo'><Text name="Opintopolku.fi"/></div>
      <h1>
        <Link href="/koski/"><Text name="Koski"/></Link>
        <span>{' - '}<Text name="Omat tiedot"/></span>
      </h1>
      <UserInfo user={user}/>
    </header>
  )
}