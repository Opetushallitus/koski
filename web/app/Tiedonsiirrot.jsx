import React from 'react'
import Bacon from 'baconjs'
import { locationP } from './location.js'

export const tiedonsiirrotContentP = contentP => Bacon.combineWith(contentP, locationP, (content, location) => (<div className='content-area'>
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
  return <span className={className}><a href={path}>{text}</a></span>
}