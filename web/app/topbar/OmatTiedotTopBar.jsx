import React from 'baret'
import {UserInfo} from './UserInfo'
import Link from '../Link'
import Text from '../Text'

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