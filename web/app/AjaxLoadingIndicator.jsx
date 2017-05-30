import React from 'react'

const loadingElement = (text) => <div className="ajax-loading-placeholder">{text}</div>
const loadingContent = (text) => ({ title: 'Ladataan...', content: loadingElement(text) })

export const contentWithLoadingIndicator = (contentP, text = 'Ladataan...') => contentP.startWith(loadingContent(text))
export const elementWithLoadingIndicator = (elementP, text) => elementP.startWith(loadingElement(text))