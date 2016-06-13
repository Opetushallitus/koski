function TodistusPage() {
  function getText(selector) {
    return S(selector).text().replace(/\s+/g, ' ').trim()
  }
  var api = {
    isVisible: function() {
      return isElementVisible(S('body>div.todistus'))
    },
    arvosanarivi: getText,
    headings: function() {
      return getText('h1,h2,h3')
    },
    vahvistus: function() {
      return getText('.vahvistus')
    },
    close: function() {
      if (api.isVisible()) {
        testFrame().history.go(-1)
      }
    }
  }

  return api
}