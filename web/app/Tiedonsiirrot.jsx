import React from 'react'
import Bacon from 'baconjs'
import {navigateTo} from './location.js'

const loadingContent = <div className="ajax-indicator-bg">Ladataan...</div>

const withLoadingIndicator = (contentP) => Bacon.once(loadingContent).concat(contentP).toProperty()

export const tiedonsiirrotContentP = (location, contentP) => withLoadingIndicator(contentP).map((content) => (<div className='content-area'>
  <nav className="sidebar tiedonsiirrot-navi">
    {link('/koski/tiedonsiirrot', 'Tiedonsiirtoloki', location, 'tiedonsiirto-link')}
    {link('/koski/tiedonsiirrot/virheet', 'Virheet', location, 'virheet-link')}
  </nav>
  <div className="main-content tiedonsiirrot-content">
    { content }
  </div>
</div>))

const link = (path, text, location, linkClassName) => {
  const className = path == location ? 'navi-link-container selected' : 'navi-link-container'
  return (<span className={className}><a href={path} className={linkClassName} onClick={(e) => {
    e.preventDefault()
    navigateTo(path)
  }}>{text}</a></span>)
}