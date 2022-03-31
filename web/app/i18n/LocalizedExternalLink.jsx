import React from 'react'
import {lang} from './i18n'
import ExternalLinkIcon from '../omadata/luvanhallinta/ExternalLinkIcon'

export const LocalizedExternalLink = ({options, children}) => (
  <a target="_blank" href={options[lang] || options['fi']} rel="noopener noreferrer">
    {children}
    {' '}
    <ExternalLinkIcon />
  </a>
)

LocalizedExternalLink.displayName = 'LocalizedExternalLink'
