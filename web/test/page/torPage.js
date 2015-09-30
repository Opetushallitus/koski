function TorPage() {
  var pageApi = Page(function() {return S("#content")});

  var api = {
    openPage: function() {
      return openPage("/tor/", api.isVisible)()
    },
    isVisible: function() {
      return S("#content .oppija-haku").is(":visible")
    },
    loginAndOpen: function() {
      return Authentication().login().then(api.openPage)
    },
    search: function(query, expectedResults) {
      if (typeof expectedResults != "function") {
        var expectedNumberOfResults = expectedResults
        expectedResults = function() {
          return api.getSearchResults().length == expectedNumberOfResults
        }
      }
      return function() {
        return pageApi.setInputValue("#search-query", query)()
          .then(wait.until(expectedResults))
      }
    },
    getSearchResults: function() {
      return S('.oppija-haku li a').toArray().map(function(a) { return $(a).text()})
    },
    addNewOppija: function() {
      triggerEvent(S('.oppija-haku .lisaa-oppija'), "click")
      return wait.until(AddOppijaPage().isVisible)
    },
    isNoResultsLabelShown: function() {
      return S('.oppija-haku .no-results').is(":visible")
    },
    getSelectedOppija: function() {
      return S('.oppija').text()
    },
    selectOppija: function(oppija) {
      return function() {
        triggerEvent(S(S('.oppija-haku li a').toArray().filter(function(a) { return $(a).text().indexOf(oppija) > -1 })[0]), 'click')
        return api.waitUntilOppijaSelected(oppija)
      }
    },
    waitUntilOppijaSelected: function(oppija) {
      return wait.until(api.isOppijaSelected(oppija))()
    },
    isOppijaSelected: function(oppija) {
      return function() {
        return api.getSelectedOppija().indexOf(oppija) >= 0
      }
    },
    logout: function() {
      triggerEvent(S("#logout"), 'click')
      return wait.until(LoginPage().isVisible)()
    }
  }
  return api
}