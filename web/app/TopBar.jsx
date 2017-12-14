import React from 'baret'
import {UserInfo} from './UserInfo.jsx'
import Link from './Link.jsx'
import Text from './Text.jsx'
import {naviLink} from './Tiedonsiirrot.jsx'
import {OpiskeluoikeusInvalidatedMessage} from './OpiskeluoikeusInvalidation.jsx'

export const TopBar = ({user, titleKey, inRaamit, location}) => {
  return (inRaamit
    ? <RaamitTopBar location={location} user={user}/>
    : <LocalTopBar location={location} user={user} titleKey={titleKey}/>
  )
}

const NavList = ({location, user}) => {
  if (!user || !user.hasAnyReadAccess) {
    return null
  }
  return (<ul>
    <li>{naviLink('/koski/virkailija', 'Opiskelijat', location.path, '', (path, loc) => loc == path || loc.startsWith('/koski/oppija'))}</li>
    <li>{naviLink('/koski/tiedonsiirrot', 'Tiedonsiirrot', location.path, '', (path, loc) => loc.startsWith(path))}</li>
    {user && user.hasGlobalReadAccess && <li>{naviLink('/koski/validointi', 'Validointi', location.path, '')}</li>}
    <li>{naviLink('/koski/dokumentaatio', 'Dokumentaatio', location.path, '')}</li>
  </ul>)
}

const RaamitTopBar = ({location, user}) => {
  return (<header id="topbar" className="inraamit topbarnav">
    <NavList location={location} user={user}/>
    <OpiskeluoikeusInvalidatedMessage location={location} />
  </header>)
}

const LocalTopBar = ({location, user, titleKey}) => {
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
