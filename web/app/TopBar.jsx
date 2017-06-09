import React from 'baret'
import {UserInfo} from './UserInfo.jsx'
import Link from './Link.jsx'
import Text from './Text.jsx'
import {editAtom, startEdit, hasEditAccess} from './i18n-edit'
import {naviLink} from './Tiedonsiirrot.jsx'

export const TopBar = ({user, saved, titleKey, inRaamit, location}) => {
  return inRaamit ?
    <RaamitTopBar location={location}/> :
    <LocalTopBar user={user} saved={saved} titleKey={titleKey}/>
}

const RaamitTopBar = ({location}) => {
  return (
    <header id="topbar">
      {naviLink('/koski/', 'Opiskelijat', location.path, '')}
      <span className="separator"/>
      {naviLink('/koski/tiedonsiirrot', 'Tiedonsiirrot', location.path, '', (path, loc) => loc.startsWith(path))}
    </header>
  )
}

const LocalTopBar = ({user, saved, titleKey}) => {
  let showEdit = hasEditAccess.and(editAtom.not())
  let onClick = e => {
    startEdit()
    e.stopPropagation()
    e.preventDefault()
  }

  return (
    <header id='topbar' className={saved ? 'saved' : ''}>
      <div id='logo'><Text name="Opintopolku.fi"/></div>
      <h1>
        <Link href="/koski/"><Text name="Koski"/></Link>
        {titleKey ? <span>{' - '}<Text name={titleKey}/></span> : ''}
      </h1>
      {showEdit.map((show) => show && <a className="edit-localizations" onClick={onClick}>{""}</a>)}
      <span className="save-info"><Text name="Kaikki muutokset tallennettu." edit="false"/></span>
      <UserInfo user={user}/>
    </header>
  )
}
