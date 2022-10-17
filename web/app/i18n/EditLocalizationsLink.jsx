import React from 'baret'
import { startEdit, hasEditAccess } from './i18n-edit'
import Text from './Text'

export const EditLocalizationsLink = () => {
  const showEdit = hasEditAccess
  const onClick = (e) => {
    startEdit()
    e.stopPropagation()
    e.preventDefault()
  }

  return (
    <span>
      {showEdit.map(
        (show) =>
          show && (
            <a className="edit-localizations" onClick={onClick}>
              <Text name="Muokkaa käännöksiä" />
            </a>
          )
      )}
    </span>
  )
}
