function OpintosuoritusotePage() {
  function getText(selector) {
    return S(selector).text().replace(/\s+/g, ' ').trim()
  }
  return {
    isVisible: function() {
      return isElementVisible(S('body.opintosuoritusote'))
    },
    arvosanarivi: getText,
    headings: function() {
      return getText('h1,h2,h3')
    },
    vahvistus: function() {
      return getText('.vahvistus')
    }
  }
}