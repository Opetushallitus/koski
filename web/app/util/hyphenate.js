const words = [
  // arvosana with 10+ characters
  'appro-batur',
  'elemen-tary',
  'erin-omainen',
  'hyväk-sytty',
  'impro-batur',
  'kiitet-tävä',
  'kohta-lainen',
  'osallis-tunut',
  'satis-factory',
  'tyydyt-tävä',
  'tyydyt-tävät',
  // laajuus with 10+ characters
  'opinto-viikkoa',
  'studie-veckor',
  'vuosi-viikkotuntia',
  'årsvecko-timmar',
  'osaamis-pistettä',
  'kompetens-poäng',
  'compe-tence'
]

const regexps = words.map((word) => {
  const parts = word.split('-')
  return new RegExp(`\\b(${parts[0]})(${parts[1]})\\b`, 'i')
})

// return string with possibly soft hyphens (Unicode 00AD) inserted at suitable places
// intended mainly for table columns that should be as narrow as possible on mobile
export const hyphenate = (s) => {
  return regexps.reduce((acc, val) => acc.replace(val, '$1\u00ad$2'), s)
}
