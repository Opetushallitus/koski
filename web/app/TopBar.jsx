import React from 'baret'
import {UserInfo} from './UserInfo.jsx'
import Link from './Link.jsx'
import Text from './Text.jsx'
import {naviLink} from './Tiedonsiirrot.jsx'

export const TopBar = ({user, saved, titleKey, inRaamit, location}) => {
  return inRaamit ?
    <RaamitTopBar location={location}/> :
    <LocalTopBar location={location} user={user} saved={saved} titleKey={titleKey}/>
}

const RaamitTopBar = ({location}) => {
  return (
    <header id="topbar" className="inraamit topbarnav">
      <ul>
        <li>{naviLink('/koski/', 'Opiskelijat', location.path, '')}</li>
        <li>{naviLink('/koski/tiedonsiirrot', 'Tiedonsiirrot', location.path, '', (path, loc) => loc.startsWith(path))}</li>
      </ul>
    </header>

  )
}

const LocalTopBar = ({location, user, saved, titleKey}) => {
  return (
    <header id='topbar' className={'local' + (saved ? 'saved' : '')}>
      <div id='logo'><Text name="Opintopolku.fi"/></div>
      <h1>
        <Link href="/koski/"><Text name="Koski"/></Link>
        {titleKey ? <span>{' - '}<Text name={titleKey}/></span> : ''}
      </h1>
      <span className="save-info"><Text name="Kaikki muutokset tallennettu." edit="false"/></span>
      <UserInfo user={user}/>
      {(user !== null) &&
        <div className='topbarnav'>
          <ul>
            <li>{naviLink('/koski/', 'Opiskelijat', location.path, '')}</li>
            <li>{naviLink('/koski/tiedonsiirrot', 'Tiedonsiirrot', location.path, '', (path, loc) => loc.startsWith(path))}</li>
          </ul>
        </div>
      }
    </header>
  )
}
