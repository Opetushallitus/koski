import React from 'react'

const loadingElement = <div className="ajax-indicator-bg">Ladataan...</div>
const loadingContent = { title: 'Ladataan...', content: loadingElement } // TODO: i18n

export const contentWithLoadingIndicator = (contentP) => contentP.startWith(loadingContent)
export const elementWithLoadingIndicator = (contentP) => contentP.startWith(loadingElement)