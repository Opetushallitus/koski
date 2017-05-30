import React from 'react'
import { t } from './i18n'

export default ({name, ignoreMissing}) => {
  return <span className="localized">{t(name, ignoreMissing)}</span>
}