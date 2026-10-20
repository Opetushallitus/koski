import React from 'react'
import Highlighter from 'react-highlight-words'

export type HighlightProps = {
  search: string
  ignoreDiacritics?: boolean
  children?: string
}

export const Highlight: React.FC<HighlightProps> = ({
  search,
  ignoreDiacritics,
  children
}) => (
  <Highlighter
    searchWords={[search]}
    textToHighlight={children ?? ''}
    highlightClassName="highlight"
    sanitize={ignoreDiacritics ? removeDiacriticsExceptÅÄÖ : undefined}
  />
)

// Keeps åäö distinct like the finnish_folding analyzer of the perustiedot OpenSearch index
const removeDiacriticsExceptÅÄÖ = (text: string): string =>
  Array.from(text.normalize('NFC'), (c) =>
    'åäöÅÄÖ'.includes(c)
      ? c
      : c.normalize('NFD').replace(/[\u0300-\u036f]/g, '')
  ).join('')
