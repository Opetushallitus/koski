import React from 'baret'
import LocalTopBar from './LocalTopBar'
import NavList from './NavList'
import InvalidationNotification from '../components/InvalidationNotification'

export const TopBar = ({user, titleKey, inRaamit, location}) => {
  return (inRaamit
    ? <RaamitTopBar location={location} user={user}/>
    : <LocalTopBar location={location} user={user} titleKey={titleKey}/>
  )
}

TopBar.displayName = 'TopBar'

const RaamitTopBar = ({location, user}) => {
  return (<header id="topbar" className="inraamit topbarnav">
    <NavList location={location} user={user}/>
    <InvalidationNotification/>
  </header>)
}

RaamitTopBar.displayName = 'RaamitTopBar'
