function KelaPage() {

  var pageApi = Page(function() {return S('#content')})

  var api = {
    openPage: function() {
      return openPage("/koski/kela", api.isReady)()
    },
    isVisible: function() {
      return isElementVisible(S('#content .kela'))
    },
    isReady: function() {
      return api.isVisible() && !isLoading()
    },
    getOppijanNimi: function () {
      return S('h2.henkilotiedot').text()
    },
    getValittuOpiskeluoikeusOtsikko: function () {
      return S('h3.otsikko').text()
    },
    setSearchInputValue: function (value) {
      return function() {
        return pageApi.setInputValue('#kela-search-query', value)()
      }
    },
    searchAndSelect: function (hetu) {
      return function() {
        return api.setSearchInputValue(hetu)()
          .then(wait.forAjax)
          .then(wait.until(function () { return isElementVisible(S('.opiskeluoikeus-tabs'))}))
      }
    },
    selectOpiskeluoikeusByTyyppi: function (opiskeluoikeudenTyyppi) {
      return function () {
        var opiskeluoikeudet = S('.opiskeluoikeus-tabs > ul > li').toArray()
        var opiskeluoikeus = opiskeluoikeudet.find(function(li) { return $(li).text().includes(opiskeluoikeudenTyyppi)})
        return click(opiskeluoikeus)()
      }
    },
    selectSuoritus: function (suoritusTabNimi) {
      return function () {
        var suoritukset = S('.suoritukset > .tabs > ul > li').toArray()
        var suoritus = suoritukset.find(function (li) { return $(li).text().includes(suoritusTabNimi)})
        return click(suoritus)()
      }
    },
    selectOsasuoritus: function (osasuoritukseNimi) {
      return function () {
        var osasuoritukset = S('tr.osasuoritukset > td > span.suorituksen-nimi').toArray()
        var osasuoritus = osasuoritukset.find(function (li) { return $(li).text().includes(osasuoritukseNimi)})
        return click(osasuoritus)()
      }
    },
  }
  return api
}
