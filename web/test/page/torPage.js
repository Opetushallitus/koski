function TorPage() {
  var pageApi = Page(function() {return S('#content')})

  var OppijaHaku = {
    search: function(query, expectedResults) {
      if (expectedResults instanceof Array) {
        var resultList = expectedResults
        expectedResults = function() {
          return _.eq(resultList, OppijaHaku.getSearchResults())
        }
      }
      if (typeof expectedResults != 'function') {
        var expectedNumberOfResults = expectedResults
        expectedResults = function() {
          return OppijaHaku.getSearchResults().length == expectedNumberOfResults
        }
      }
      return function() {
        return pageApi.setInputValue('#search-query', query)()
          .then(wait.until(expectedResults))
      }
    },
    getSearchResults: function() {
      return S('.oppija-haku li a').toArray().map(function(a) { return $(a).text()})
    },
    addNewOppija: function() {
      triggerEvent(S('.oppija-haku .lisaa-oppija'), 'click')
      return wait.until(AddOppijaPage().isVisible)()
    },
    isNoResultsLabelShown: function() {
      return S('.oppija-haku .no-results').is(':visible')
    },
    getSelectedSearchResult: function() {
      return S('.hakutulokset .selected').text()
    },
    selectOppija: function(oppija) {
      return function() {
        triggerEvent(S(S('.oppija-haku li a').toArray().filter(function(a) { return $(a).text().indexOf(oppija) > -1 })[0]), 'click')
        return api.waitUntilOppijaSelected(oppija)
      }
    }
  }

  var api = {
    openPage: function() {
      return openPage('/tor/', api.isVisible)()
    },
    isVisible: function() {
      return S('#content .oppija-haku').is(':visible')
    },
    isLoading: function() {
      return S('body').hasClass('loading')
    },
    isNotLoading: function() {
      return !api.isLoading()
    },
    isReady: function() {
      return api.isVisible() && !api.isLoading()
    },
    loginAndOpen: function() {
      return Authentication().login('kalle')().then(api.openPage)
    },
    oppijaHaku: OppijaHaku,
    getSelectedOppija: function() {
      return S('.oppija h2').text()
    },
    waitUntilOppijaSelected: function(oppija) {
      return wait.until(api.isOppijaSelected(oppija))()
    },
    isOppijaSelected: function(oppija) {
      return function() {
        return api.getSelectedOppija().indexOf(oppija) >= 0 && OppijaHaku.getSelectedSearchResult().indexOf(oppija) >= 0
      }
    },
    logout: function() {
      triggerEvent(S('#logout'), 'click')
      return wait.until(LoginPage().isVisible)()
    },
    isErrorShown: function() {
      return S("#error.error").is(":visible")
    },
    is404: function() {
      return S(".not-found").is(":visible")
    },
    isSavedLabelShown: function() {
      return S('.saved').is(':visible')
    },
    getUserName() {
      return S('.user-info .name').text()
    }
  }

  return api
}