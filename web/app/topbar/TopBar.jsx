import React from 'baret'
import {OpiskeluoikeusInvalidatedMessage} from '../OpiskeluoikeusInvalidation.jsx'
import LocalTopBar from './LocalTopBar.jsx'
import NavList from './NavList.jsx'

export const TopBar = ({user, titleKey, inRaamit, location}) => {
  return (inRaamit
    ? <RaamitTopBar location={location} user={user}/>
    : <LocalTopBar location={location} user={user} titleKey={titleKey}/>
  )
}

const RaamitTopBar = ({location, user}) => {
  return (<header id="topbar" className="inraamit topbarnav">
    <NavList location={location} user={user}/>
    <OpiskeluoikeusInvalidatedMessage location={location} />
  </header>)
}