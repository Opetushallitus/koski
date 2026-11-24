// Leikepöydän kautta tekstiin päätyy usein välilyönniltä näyttäviä merkkejä (esim. U+00A0), joita JSON ei hyväksy
const unicodeSpaces = /[\u00a0\u1680\u2000-\u200a\u202f\u205f\u3000]/g
const invisibleCharacters = /[\u200b\ufeff]/g

export type ParsedQuery =
  { ok: true; value: unknown } | { ok: false; error: string }

const describeCharacterAt = (text: string, position: number): string => {
  const codePoint = text.codePointAt(position)
  if (codePoint === undefined) return ''
  const hex = codePoint.toString(16).toUpperCase().padStart(4, '0')
  return ` (merkki U+${hex} kohdassa ${position})`
}

export const parseQuery = (text: string): ParsedQuery => {
  const normalized = text
    .replace(unicodeSpaces, ' ')
    .replace(invisibleCharacters, '')
  try {
    return { ok: true, value: JSON.parse(normalized) }
  } catch (e) {
    const position = /position (\d+)/.exec(String(e))
    return {
      ok: false,
      error:
        String(e) +
        (position ? describeCharacterAt(normalized, Number(position[1])) : '')
    }
  }
}
