import React from 'baret'
import {UserInfo} from './UserInfo.jsx'
import Link from './Link.jsx'
import Text from './Text.jsx'
import {naviLink} from './Tiedonsiirrot.jsx'

export const TopBar = ({user, titleKey, inRaamit, location}) => {
  return inRaamit ?
    <RaamitTopBar location={location}/> :
    <LocalTopBar location={location} user={user} titleKey={titleKey}/>
}

const RaamitTopBar = ({location}) => {
  return (
    <header id="topbar" className="inraamit topbarnav">
      {navLinks(location)}
    </header>
  )
}

const LocalTopBar = ({location, user, titleKey}) => {
  return (
    <header id='topbar' className="local">
      <div id='logo'><Text name="Opintopolku.fi"/></div>
      <h1>
        <Link href="/koski/"><Text name="Koski"/></Link>
        {titleKey ? <span>{' - '}<Text name={titleKey}/></span> : ''}
      </h1>
      <UserInfo user={user}/>
      {(user !== null) &&
        <div className='topbarnav'>
          {navLinks(location)}
        </div>
      }
    </header>
  )
}

const navLinks = (location) => (<ul>
    <li>{naviLink('/koski/', 'Opiskelijat', location.path, '')}</li>
    <li>{naviLink('/koski/tiedonsiirrot', 'Tiedonsiirrot', location.path, '', (path, loc) => loc.startsWith(path))}</li>
    <li>{naviLink('/koski/validointi', 'Validointi', location.path, '')}</li>
  </ul>)