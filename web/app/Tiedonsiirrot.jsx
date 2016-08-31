import React from 'react'
import {navigateTo} from './location.js'

export const tiedonsiirrotContentP = (location, contentP) => contentP.map((content) => (<div className='content-area'>
  <nav className="sidebar tiedonsiirrot-navi">
    {link('/koski/tiedonsiirrot', 'Tiedonsiirtoloki', location)}
    {link('/koski/tiedonsiirrot/virheet', 'Virheet', location)}
  </nav>
  <div className="main-content tiedonsiirrot-content">
    { content }
  </div>
</div>))

const link = (path, text, location) => {
  const className = path == location ? 'navi-link-container selected' : 'navi-link-container'
  return (<span className={className}><a href={path} onClick={(e) => {
    e.preventDefault()
    navigateTo(path)
  }}>{text}</a></span>)
}