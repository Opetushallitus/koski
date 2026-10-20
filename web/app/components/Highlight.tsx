import React from 'react'
import Highlighter from 'react-highlight-words'

export type HighlightProps = {
  searchQuery: string
  finnishFolding?: boolean
  children?: string
}

export const Highlight: React.FC<HighlightProps> = ({
  searchQuery,
  finnishFolding = true,
  children
}) => (
  <Highlighter
    searchWords={[searchQuery]}
    textToHighlight={children ?? ''}
    highlightClassName="highlight"
    sanitize={finnishFolding ? foldFinnish : undefined}
  />
)

// Folds text like the finnish_folding filter of the perustiedot OpenSearch index
// (OpiskeluoikeudenPerustiedotIndexer), so that the highlight agrees with the search:
// - å, ä and ö are letters of their own in Finnish and stay distinct from a and o
// - accents are removed from other letters: é → e, š → s
// - ligatures and full-width forms are split: ﬁ → fi, Ｆ → F
// - letters that have no accent to remove are mapped by hand: ø → o, æ → ae, ß → ss
// - case is ignored, as OpenSearch lowercases both the index and the query
// - a decomposed ä (a + combining dots) folds to a, as OpenSearch does not normalize it either
// ICU folding also maps about a hundred rare Latin letters (ƀ, ȸ, ǁ…) that are left out here.
export const foldFinnish = (text: string): string =>
  Array.from(text.toLowerCase(), foldLetter).join('')

const foldLetter = (letter: string): string =>
  'åäö'.includes(letter)
    ? letter
    : Array.from(
        // NFKD splits é into e + a combining acute and ﬁ into f + i; the range is the
        // Combining Diacritical Marks block, so this keeps the base letters only
        letter.normalize('NFKD').replace(/[\u0300-\u036f]/g, ''),
        (c) => standaloneLetterFolds[c] ?? c
      ).join('')

const standaloneLetterFolds: Record<string, string> = {
  æ: 'ae',
  ð: 'd',
  đ: 'd',
  ħ: 'h',
  ı: 'i',
  ł: 'l',
  ŋ: 'n',
  ø: 'o',
  œ: 'oe',
  ß: 'ss',
  þ: 'th',
  ŧ: 't'
}
