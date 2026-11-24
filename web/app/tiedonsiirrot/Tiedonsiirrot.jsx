import React from 'react'
import { contentWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import Text from '../i18n/Text'
import { onlyIfHasReadAccess } from '../virkailija/accessCheck'
import { TiedonsiirrotNavi } from './TiedonsiirrotNavi'

export const tiedonsiirrotContentP = (location, contentP) =>
  onlyIfHasReadAccess(
    contentWithLoadingIndicator(contentP).map((content) => ({
      content: (
        <div className="content-area tiedonsiirrot">
          <TiedonsiirrotNavi location={location} />
          <div className="main-content tiedonsiirrot-content">
            {content.content}
          </div>
        </div>
      ),
      title: content.title
    }))
  )

export const ReloadButton = () => (
  <button
    className="koski-button update-content"
    onClick={() => window.location.reload(true)}
  >
    <Text name="Päivitä" />
  </button>
)
