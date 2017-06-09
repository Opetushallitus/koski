import React from 'react'
import Link from './Link.jsx'
import { contentWithLoadingIndicator } from './AjaxLoadingIndicator.jsx'
import Text from './Text.jsx'

export const tiedonsiirrotContentP = (location, contentP) => contentWithLoadingIndicator(contentP).map((content) => ({
  content:  (<div className='content-area tiedonsiirrot'>
              <nav className="sidebar tiedonsiirrot-navi">
                {naviLink('/koski/tiedonsiirrot/yhteenveto', 'Yhteenveto', location, 'yhteenveto-link')}
                {naviLink('/koski/tiedonsiirrot', 'Tiedonsiirtoloki', location, 'tiedonsiirto-link')}
                {naviLink('/koski/tiedonsiirrot/virheet', 'Virheet', location, 'virheet-link')}
              </nav>
              <div className="main-content tiedonsiirrot-content">
                { content.content }
              </div>
            </div>),
  title: content.title
}))

export const naviLink = (path, textKey, location, linkClassName, isSelected = (p, l) => p === l) => {
  const className = isSelected(path, location) ? 'navi-link-container selected' : 'navi-link-container'
  return (<span className={className}><Link href={path} className={linkClassName}><Text name={textKey}/></Link></span>)
}