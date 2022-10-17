function KoskiPage() {
  var pageApi = Page(function () {
    return S('#content')
  })

  var OppijaHaku = {
    search: function (query, expectedResults) {
      if (!expectedResults) expectedResults = query
      if (expectedResults instanceof Array) {
        var resultList = expectedResults
        expectedResults = function () {
          return _.isEqual(resultList, OppijaHaku.getSearchResults())
        }
      } else if (typeof expectedResults === 'string') {
        var expectedString = expectedResults
        expectedResults = function () {
          var results = OppijaHaku.getSearchResults()
          return (
            results.length == 1 &&
            results[0].toLowerCase().indexOf(expectedString.toLowerCase()) >= 0
          )
        }
      } else if (typeof expectedResults !== 'function') {
        throw new Error('either function, array or string expected')
      }
      return function () {
        return pageApi
          .setInputValue('#search-query', query)()
          .then(wait.forAjax)
          .then(wait.until(not(OppijaHaku.isSearchInProgress)))
          .then(wait.until(expectedResults))
      }
    },
    searchAndSelect: function (query, name) {
      if (!name) {
        name = query
      }
      return function () {
        return OppijaHaku.search(query, name)().then(
          OppijaHaku.selectOppija(name)
        )
      }
    },
    getSearchResults: function () {
      return S('.oppija-haku li a')
        .toArray()
        .map(function (a) {
          return $(a).text()
        })
    },
    getSearchString: function () {
      return pageApi.getInputValue('#search-query')
    },
    canAddNewOppija: function () {
      var button = S('.oppija-haku .lisaa-oppija')
      return button.is(':visible') && !button.hasClass('disabled')
    },
    addNewOppija: seq(
      click(findSingle('.oppija-haku .lisaa-oppija')),
      wait.until(AddOppijaPage().isVisible),
      wait.forAjax
    ),
    isNoResultsLabelShown: function () {
      return isElementVisible(S('.oppija-haku .no-results'))
    },
    getSelectedSearchResult: function () {
      return S('.hakutulokset .selected').text()
    },
    isSearchInProgress: function () {
      return S('.oppija-haku').hasClass('searching')
    },
    selectOppija: function (oppija) {
      var link = findSingle('.oppija-haku li a:contains(' + oppija + ')')
      return seq(click(link), api.waitUntilOppijaSelected(oppija))
    },
    getErrorMessage: function () {
      return S('.oppija-haku .error').text()
    }
  }

  var Oppijataulukko = {
    isVisible: function () {
      return isElementVisible(Oppijataulukko.tableElem())
    },
    findOppija: function (nimi, tyyppi) {
      return textsOf(
        S('tr:contains(' + nimi + '):contains(' + tyyppi + ')').find('td')
      )
    },
    data: function () {
      return Oppijataulukko.tableElem()
        .find('tbody tr')
        .toArray()
        .map(function (row) {
          return textsOf($(row).find('td'))
        })
    },
    names: function () {
      return Oppijataulukko.data().map(function (row) {
        return row[0]
      })
    },
    oppilaitokset: function () {
      return Oppijataulukko.data().map(function (row) {
        return row[5]
      })
    },
    highlights: function () {
      return textsOf(S('.highlight'))
    },
    isReady: function () {
      return Oppijataulukko.isVisible() && !isLoading()
    },
    filterBy: function (className, value) {
      return function () {
        if (
          className == 'nimi' ||
          className == 'tutkinto' ||
          className == 'luokka'
        ) {
          return Page(Oppijataulukko.tableElem)
            .setInputValue('th.' + className + ' input', value || '')()
            .then(wait.forMilliseconds(500))
            .then(wait.forAjax) // <- TODO 500ms throttle in input is slowing tests down
        } else if (className == 'oppilaitos') {
          return OrganisaatioHaku(Oppijataulukko.tableElem).select(value)()
        } else if (
          className == 'alkamispäivä' ||
          className == 'päättymispäivä'
        ) {
          return seq(
            click(S('.' + className + ' .date-range-selection')),
            Page(Oppijataulukko.tableElem).setInputValue(
              '.date-range-input input.end',
              value || ''
            ),
            click('body')
          )()
        } else {
          return Page(Oppijataulukko.tableElem)
            .setInputValue(
              'th.' + className + ' .dropdown',
              value || 'Ei valintaa'
            )()
            .then(wait.forAjax)
        }
      }
    },
    sortBy: function (className) {
      return click('.' + className + ' .sorting')
    },
    tableElem: function () {
      return S('#content .oppijataulukko')
    },
    clickFirstOppija: function () {
      return click(
        findSingle('.oppijataulukko tbody:eq(0) tr:eq(0) td.nimi a')
      )()
    }
  }

  var api = {
    openPage: function () {
      return openPage('/koski/virkailija', api.isReady)()
    },
    openFromMenu: function () {
      return click(findSingle('.opiskelijat.navi-link-container'))
    },
    isVisible: function () {
      return (
        isElementVisible(S('#content .oppija-haku')) ||
        isElementVisible(S('#content .oppija'))
      )
    },
    isReady: function () {
      return api.isVisible() && !isLoading()
    },
    loginAndOpen: function () {
      return Authentication().login('kalle')().then(api.openPage)
    },
    oppijaHaku: OppijaHaku,
    oppijataulukko: Oppijataulukko,
    getSelectedOppija: function () {
      return S('.oppija h2').text().replace('JSON', '')
    },
    opiskeluoikeudeTotal: function () {
      return S('.opiskeluoikeudet-total .value').text().slice(2)
    },
    waitUntilOppijaSelected: function (oppija) {
      return wait.until(api.isOppijaSelected(oppija))
    },
    waitUntilAnyOppijaSelected: function () {
      return wait.until(function () {
        return api.getSelectedOppija().length > 0
      })
    },
    isOppijaSelected: function (oppija) {
      return function () {
        return api.getSelectedOppija().indexOf(oppija) >= 0 // || OppijaHaku.getSelectedSearchResult().indexOf(oppija) >= 0
      }
    },
    isOppijaLoading: function () {
      return isElementVisible(S('.oppija.loading'))
    },
    logout: seq(click('#logout'), wait.until(LoginPage().isVisible)),
    isErrorShown: function () {
      return isElementVisible(S('#error.error')) || api.isTopLevelError()
    },
    getErrorMessage: function () {
      return S('#error.error .error-text, .error-message').text()
    },
    verifyNoError: function () {
      function checkError() {
        if (api.isErrorShown()) {
          throw new Error('Error shown on page: ' + api.getErrorMessage())
        }
      }
      checkError()
      return wait.forAjax().then(checkError)
    },
    is403: function () {
      return isElementVisible(S('.http-status:contains(403)'))
    },
    is404: function () {
      return isElementVisible(S('.http-status:contains(404)'))
    },
    is500: function () {
      return isElementVisible(S('.http-status:contains(500)'))
    },
    isTopLevelError: function () {
      return isElementVisible(S('.content-area.error'))
    },
    isSavedLabelShown: function () {
      return isElementVisible(S('.saved'))
    },
    getUserName: function () {
      return S('.user-info .name').text()
    },
    isOpiskeluoikeusInvalidatedMessageShown: function () {
      return (
        isElementVisible(S('.invalidation-notification')) &&
        !isElementVisible(S('.invalidation-notification.hide')) &&
        S('.invalidation-notification').text() == 'Opiskeluoikeus mitätöity'
      )
    },
    isPäätasonSuoritusDeletedMessageShown: function () {
      return (
        isElementVisible(S('.invalidation-notification')) &&
        !isElementVisible(S('.invalidation-notification.hide')) &&
        S('.invalidation-notification').text() == 'Suoritus poistettu'
      )
    }
  }

  return api
}

function prepareForNewOppija(username, hetu) {
  var page = KoskiPage()
  return function () {
    return Authentication()
      .login(username)()
      .then(resetFixtures)
      .then(page.openPage)
      .then(page.oppijaHaku.search(hetu, page.oppijaHaku.isNoResultsLabelShown))
      .then(wait.until(page.oppijaHaku.canAddNewOppija))
      .then(page.oppijaHaku.addNewOppija)
      .then(wait.forAjax)
  }
}
