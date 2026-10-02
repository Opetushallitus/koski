const hljs = require('highlight.js/lib/core')

hljs.registerLanguage('css', require('highlight.js/lib/languages/css'))
hljs.registerLanguage(
  'markdown',
  require('highlight.js/lib/languages/markdown')
)
hljs.registerLanguage('json', require('highlight.js/lib/languages/json'))
hljs.registerLanguage(
  'javascript',
  require('highlight.js/lib/languages/javascript')
)
hljs.registerLanguage('xml', require('highlight.js/lib/languages/xml'))

module.exports = hljs
