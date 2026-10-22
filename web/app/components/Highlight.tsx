import React, { useMemo } from 'react'
import Highlighter, { Chunk } from 'react-highlight-words'

// How the search matched the text, so that the highlight agrees with the search
export type HighlightMatching = 'wordPrefix' | 'textPrefix' | 'substring'

export type HighlightProps = {
  searchQuery: string
  matching: HighlightMatching
  children?: string
}

export const Highlight: React.FC<HighlightProps> = ({
  searchQuery,
  matching,
  children
}) => {
  const findMatches = useMemo(
    () => createMatcher(searchQuery, matching),
    [searchQuery, matching]
  )

  return (
    <Highlighter
      // The whole search query is the only search word, as all matches are styled the same
      searchWords={[searchQuery]}
      textToHighlight={children ?? ''}
      highlightClassName="highlight"
      findChunks={
        findMatches
          ? ({ textToHighlight }) => findMatches(textToHighlight)
          : undefined
      }
    />
  )
}

const createMatcher = (
  searchQuery: string,
  matching: HighlightMatching
): ((text: string) => Chunk[]) | undefined => {
  switch (matching) {
    case 'wordPrefix':
      return createWordPrefixMatcher(searchQuery)
    case 'textPrefix':
      return createTextPrefixMatcher(searchQuery)
    case 'substring':
      // The case-insensitive matching of react-highlight-words
      return undefined
  }
}

// Matches like the luokkahaku prefix query on the lowercased luokka.keyword of the perustiedot
// OpenSearch index: the search query is matched as a prefix of the whole text.
export const createTextPrefixMatcher = (
  searchQuery: string
): ((text: string) => Chunk[]) => {
  const lowerCaseSearchQuery = searchQuery.trim().toLowerCase()
  if (lowerCaseSearchQuery.length === 0) {
    return () => []
  }

  return (text) =>
    text.slice(0, lowerCaseSearchQuery.length).toLowerCase() ===
    lowerCaseSearchQuery
      ? [{ start: 0, end: lowerCaseSearchQuery.length, searchIndex: 0 }]
      : []
}

// Matches like the prefix queries on the perustiedot OpenSearch index
// (OpiskeluoikeudenPerustiedotRepository): the index and the query are split into words,
// and each word of the query is matched as a prefix of any word of the text. Whole words
// matched one after another are highlighted as one, together with what separates them, so
// that a hetu search highlights 010101-123N and not 010101 and 123N.
export const createWordPrefixMatcher = (
  searchQuery: string
): ((text: string) => Chunk[]) => {
  const foldedSearchQueryWords = splitIntoWords(searchQuery).map(({ word }) =>
    foldLettersLikeOpenSearch(word)
  )
  if (foldedSearchQueryWords.length === 0) {
    return () => []
  }

  return (text) => {
    const chunks: Chunk[] = []
    let isPreviousTextWordHighlightedToEnd = false

    for (const { word: textWord, textIndex } of splitIntoWords(text)) {
      // The longest match, so that searching "ee eero" highlights all of Eero, not just Ee
      const matchLength = Math.max(
        0,
        ...foldedSearchQueryWords.map(
          (foldedSearchQueryWord) =>
            findMatchingPrefix(foldedSearchQueryWord, textWord).length
        )
      )

      if (matchLength > 0) {
        const end = textIndex + matchLength

        if (isPreviousTextWordHighlightedToEnd) {
          chunks[chunks.length - 1].end = end
        } else {
          // searchIndex points into searchWords, which holds only the search query
          chunks.push({ start: textIndex, end, searchIndex: 0 })
        }
      }

      isPreviousTextWordHighlightedToEnd = matchLength === textWord.length
    }

    return chunks
  }
}

// The word boundaries of Unicode (UAX #29) that the index's icu_tokenizer also follows
const segmenter =
  'Segmenter' in Intl
    ? // eslint-disable-next-line compat/compat -- guarded, see the fallback below
      new Intl.Segmenter('und', { granularity: 'word' })
    : undefined

const splitIntoWords = (
  text: string
): Array<{ word: string; textIndex: number }> =>
  segmenter
    ? Array.from(segmenter.segment(text))
        .filter((s) => s.isWordLike)
        .map((s) => ({ word: s.segment, textIndex: s.index }))
    : // Firefox 115 has no Intl.Segmenter. This splits the same way except
      // inside words such as O'Brien or 1.2.246, which UAX #29 keeps whole.
      Array.from(text.matchAll(/[\p{L}\p{M}\p{N}_]+/gu), (match) => ({
        word: match[0],
        textIndex: match.index
      }))

// The longest prefix of the text word that compares equal to the search query word, or an
// empty string. Compared as whole strings, as a letter can equal two (ß = ss).
const findMatchingPrefix = (
  foldedSearchQueryWord: string,
  textWord: string
): string => {
  for (let length = textWord.length; length > 0; length--) {
    const textWordPrefix = textWord.slice(0, length)
    if (
      finnishCollator.compare(
        foldLettersLikeOpenSearch(textWordPrefix),
        foldedSearchQueryWord
      ) === 0
    ) {
      return textWordPrefix
    }
  }

  return ''
}

// Compares as in Finnish, ignoring case and accents (é = e, Ｆ = F, ß = ss) but keeping å, ä
// and ö distinct from a and o. The finnish_folding filter of the perustiedot OpenSearch index
// (OpiskeluoikeudenPerustiedotIndexer) folds nearly the same way, but it removes the accents of
// every letter but a precomposed å, ä and ö, so also of ü and of an ä written as a and a
// combining mark, and it folds æ, ø, þ and ı to ae, o, th and i. The Finnish alphabet has ü,
// æ and ø as y, ä and ö. Until the index is changed to follow the Finnish alphabet,
// foldLettersLikeOpenSearch folds these as the index does, so that the highlight agrees with
// the search. Rarer letters, such as ə and those of Latin Extended-B, IPA and the Cyrillic
// letters with a descender, are still left to the collator.
const finnishCollator = new Intl.Collator('fi', {
  sensitivity: 'base',
  usage: 'search'
})

const foldLettersLikeOpenSearch = (text: string): string =>
  text
    .toLowerCase()
    .replace(/[^åäö]+/gu, (letters) =>
      letters.normalize('NFKD').replace(/\p{M}/gu, '')
    )
    .replace(/[æøþı]/g, (letter) => openSearchLetterFolds[letter])

const openSearchLetterFolds: Record<string, string> = {
  æ: 'ae',
  ø: 'o',
  þ: 'th',
  ı: 'i'
}
