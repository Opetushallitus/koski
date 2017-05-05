import React from 'react'

const loadingElement = (text) => <div className="ajax-loading-placeholder">{text}</div>
const loadingContent = (text) => ({ title: 'Ladataan...', content: loadingElement(text) }) // TODO: i18n

export const contentWithLoadingIndicator = (contentP, text = 'Ladataan...') => contentP.startWith(loadingContent(text))
export const elementWithLoadingIndicator = (elementP, text) => elementP.startWith(loadingElement(text))