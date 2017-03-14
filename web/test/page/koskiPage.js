function KoskiPage() {
  var pageApi = Page(function() {return S('#content')})

  var OppijaHaku = {
    search: function(query, expectedResults) { // TODO: defaulttina voisi odottaa, että vähintään yksi tulos näkyy, jossa esiintyy hakusana
      if (expectedResults instanceof Array) {
        var resultList = expectedResults
        expectedResults = function() {
          return _.eq(resultList, OppijaHaku.getSearchResults())
        }
      }
      else if (typeof expectedResults != 'function') {
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
    searchAndSelect: function(query, name) {
      if (!name) {
        name = query
      }
      return function() {
        return OppijaHaku.search(query, 1)().then(OppijaHaku.selectOppija(name))
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
      return isElementVisible(S('.oppija-haku .no-results'))
    },
    getSelectedSearchResult: function() {
      return S('.hakutulokset .selected').text()
    },
    selectOppija: function(oppija) {
      return function() {
        var link = S('.oppija-haku li a:contains(' + oppija + ')')
        if (!link.length) throw new Error("Oppija ei näkyvissä: " + oppija)
        triggerEvent(link, 'click')
        return api.waitUntilOppijaSelected(oppija)()
      }
    }
  }

  var Oppijataulukko = {
    isVisible: function() {
      return isElementVisible(Oppijataulukko.tableElem())
    },
    findOppija: function(nimi, tyyppi) {
      return textsOf(S("tr:contains(" + nimi + "):contains(" + tyyppi + ")").find("td"))
    },
    data: function() {
      return Oppijataulukko.tableElem().find("tbody tr").toArray().map(function(row) { return textsOf($(row).find("td")) })
    },
    names: function() {
      return Oppijataulukko.data().map(function(row) { return row[0]})
    },
    isReady: function() {
      return Oppijataulukko.isVisible() && !isLoading()
    },
    filterBy: function(className, value) {
      return function() {
        if (className == "nimi" || className == "tutkinto" || className == "luokka") {
          return Page(Oppijataulukko.tableElem).setInputValue("th." + className + " input", value || "")().then(wait.forMilliseconds(500)).then(wait.forAjax) // <- TODO 500ms throttle in input is slowing tests down
        } else if (className == "oppilaitos") {
          triggerEvent('.organisaatio-selection', 'click')
          if(!window.callPhantom) { // workaround for focus glitch, when running in browser
            triggerEvent('.organisaatio-selection', 'click')
          }
         if (value) {
            return Page(Oppijataulukko.tableElem).setInputValue(".organisaatio-popup input", value || "")()
              .then(wait.forAjax)
              .then(function() { triggerEvent(S('.organisaatio-popup a:contains(' + value + ')').get(0), 'click') })
              .then(wait.forAjax)
          } else {
            if(!window.callPhantom) { // workaround for focus glitch, when running in browser
              triggerEvent('.organisaatio-selection', 'click')
            }
            triggerEvent(S('.organisaatio-popup .kaikki'), 'click')
            return wait.forAjax()
          }
        } else if (className == 'alkamispäivä') {
          triggerEvent(S('.date-range-selection'), 'click')
          return Page(Oppijataulukko.tableElem).setInputValue(".date-range-input input.end", value || "")()
            .then(function() { triggerEvent(S('body'), 'click') })
            .then(wait.forAjax)

        } else {
          return Page(Oppijataulukko.tableElem).setInputValue("th." + className +" .dropdown", value || "ei valintaa")().then(wait.forAjax)
        }
      }

    },
    sortBy: function(className) {
      return function() {
        triggerEvent(S('.' + className + ' .sorting'), 'click')
        return wait.forAjax()
      }
    },
    tableElem: function() {
      return S('#content .oppijataulukko')
    }

  }

  var api = {
    openPage: function() {
      return openPage('/koski/', api.isVisible)()
    },
    isVisible: function() {
      return isElementVisible(S('#content .oppija-haku')) || isElementVisible(S('#content .oppija'))
    },
    isLoading: function() {
      return S('.loading').length > 0
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
    oppijataulukko: Oppijataulukko,
    getSelectedOppija: function() {
      return S('.oppija h2').text().replace('JSON','')
    },
    waitUntilOppijaSelected: function(oppija) {
      return wait.until(api.isOppijaSelected(oppija))
    },
    isOppijaSelected: function(oppija) {
      return function() {
        return api.getSelectedOppija().indexOf(oppija) >= 0 // || OppijaHaku.getSelectedSearchResult().indexOf(oppija) >= 0
      }
    },
    isOppijaLoading: function() {
      return isElementVisible(S('.oppija.loading'))
    },
    logout: function() {
      triggerEvent(S('#logout'), 'click')
      return wait.until(LoginPage().isVisible)()
    },
    isErrorShown: function() {
      return isElementVisible(S("#error.error"))
    },
    getErrorMessage: function() {
      return S("#error.error .error-text").text()
    },
    verifyNoError: function() { 
      function checkError() {
        if (api.isErrorShown()) {
          throw new Error("Error shown on page: " + api.getErrorMessage())
        }
      }
      checkError()
      return wait.forAjax().then(checkError)
    },
    is404: function() {
      return isElementVisible(S(".http-status:contains(404)"))
    },
    isSavedLabelShown: function() {
      return isElementVisible(S('.saved'))
    },
    getUserName: function() {
      return S('.user-info .name').text()
    }
  }

  return api
}

function prepareForNewOppija(username, searchString) {
  var page = KoskiPage()
  return function() {
    return Authentication().login(username)()
      .then(resetFixtures)
      .then(page.openPage)
      .then(page.oppijaHaku.search(searchString, page.oppijaHaku.isNoResultsLabelShown))
      .then(page.oppijaHaku.addNewOppija)
  }
}
