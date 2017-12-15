import React from 'baret'
import {UserInfo} from './UserInfo.jsx'
import Link from '../Link.jsx'
import Text from '../Text.jsx'
import NavList from './NavList.jsx'
import {OpiskeluoikeusInvalidatedMessage} from '../OpiskeluoikeusInvalidation.jsx'

export default ({location, user, titleKey}) => {
  return (
    <header id='topbar' className="local">
      <div id='logo'><Text name="Opintopolku.fi"/></div>
      <h1>
        <Link href="/koski/"><Text name="Koski"/></Link>
        {titleKey ? <span>{' - '}<Text name={titleKey}/></span> : null}
      </h1>
      <UserInfo user={user}/>
      {(user !== null) &&
      <div className='topbarnav'>
        <NavList location={location} user={user}/>
        <OpiskeluoikeusInvalidatedMessage location={location} />
      </div>
      }
    </header>
  )
}