import React from 'baret'
import { t } from './i18n'

export const Paragraphs = ({ name, ignoreMissing, lang }) => {
  if (typeof name !== 'string') {
    console.error('Not a string', name)
    return <span>{'NOT A STRING'}</span>
  }

  return (
    <React.Fragment>
      {t(name, ignoreMissing, lang)
        .split('\n')
        .map((text, index) => (
          <p key={index}>{text}</p>
        ))}
    </React.Fragment>
  )
}
