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
        url: "/tor/login",
        data: JSON.stringify({username: "kalle", password: "asdf"}),
        contentType : 'application/json',
        dataType: "json"
      })).then(api.openPage)
    },
    search: function(query) {
      return function() {
        return pageApi.setInputValue("#search-query", query)()
          .then(wait.until(function() { return api.getSearchResults().some(function(val) { return val.indexOf(query) > -1 } ) }))
      }
    },
    getSearchResults: function() {
      return S('.oppija-haku li a').toArray().map(function(a) { return $(a).text()})
    },
    getSelectedOppija: function() {
      return S('.oppija').text()
    },
    selectOppija: function(oppija) {
      return function() {
        triggerEvent(S(S('.oppija-haku li a').toArray().filter(function(a) { return $(a).text().indexOf(oppija) > -1 })[0]), 'click')
        return wait.until(function() { return api.getSelectedOppija() === oppija })
      }
    }
  }
  return api
}