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
      return Q($.ajax({
        type: "POST",
        url: "/tor/user/login",
        data: JSON.stringify({username: "kalle", password: "asdf"}),
        contentType : 'application/json',
        dataType: "json"
      })).then(api.openPage)
    },
    search: function(query, expectedResults) {
      return function() {
        return pageApi.setInputValue("#search-query", query)()
          .then(wait.until(function() {
            return api.getSearchResults().length == expectedResults
          }))
      }
    },
    getSearchResults: function() {
      return S('.oppija-haku li a').toArray().map(function(a) { return $(a).text()})
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
        return wait.until(function() {
          return api.getSelectedOppija().indexOf(oppija) >= 0
        })()
      }
    }
  }
  return api
}