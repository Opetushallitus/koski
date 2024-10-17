import React from 'baret'
import { tTemplate } from './i18n'
import { buildClassNames } from '../components/classnames'

export default ({ templateName, className, ...templateArgs }) => {
  if (name === null || name === undefined) {
    return null
  }

  if (typeof templateName !== 'string') {
    console.error('Not a string', templateName)
    return <span>{'NOT A STRING'}</span>
  }

  return (
    <span
      aria-label={tTemplate(templateName, templateArgs)}
      className={buildClassNames([className, 'localized'])}
    >
      {tTemplate(templateName, templateArgs)}
    </span>
  )
}
