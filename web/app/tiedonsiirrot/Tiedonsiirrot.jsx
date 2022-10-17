import React from 'react'
import Link from '../components/Link'
import { contentWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import Text from '../i18n/Text'
import { onlyIfHasReadAccess } from '../virkailija/accessCheck'

export const tiedonsiirrotContentP = (location, contentP) =>
  onlyIfHasReadAccess(
    contentWithLoadingIndicator(contentP).map((content) => ({
      content: (
        <div className="content-area tiedonsiirrot">
          <nav className="sidebar tiedonsiirrot-navi">
            {naviLink(
              '/koski/tiedonsiirrot/yhteenveto',
              'Yhteenveto',
              location,
              'yhteenveto-link'
            )}
            {naviLink(
              '/koski/tiedonsiirrot',
              'Tiedonsiirtoloki',
              location,
              'tiedonsiirto-link'
            )}
            {naviLink(
              '/koski/tiedonsiirrot/virheet',
              'Virheet',
              location,
              'virheet-link'
            )}
          </nav>
          <div className="main-content tiedonsiirrot-content">
            {content.content}
          </div>
        </div>
      ),
      title: content.title
    }))
  )

export const naviLink = (
  path,
  textKey,
  location,
  linkClassName,
  isSelected = (p, l) => p === l
) => {
  let className =
    textKey.toLowerCase().replace(/ /g, '') + ' navi-link-container'
  className = isSelected(path, location) ? className + ' selected' : className
  return (
    <span className={className}>
      <Link href={path} className={linkClassName}>
        <Text name={textKey} />
      </Link>
    </span>
  )
}

export const ReloadButton = () => (
  <button
    className="koski-button update-content"
    onClick={() => window.location.reload(true)}
  >
    <Text name="Päivitä" />
  </button>
)
