function RaportitPage() {

  var pageApi = Page(function() {return S('.raportit')})
  var esiopetuksenTarkistusraportinElementti = 'div.main-content > div > section:nth-child(5)'

  var api = {
    openPage: function (predicate) {
      return function () {
        return openPage('/koski/raportit?tilastoraportit=true', predicate)()
      }
    },
    avaaOrganisaatioValitsin: function () {
      return seq(
        wait.untilVisible(function () {return S('.raportit-organisaatio')}),
        wait.untilVisible(function () {return S('.organisaatio')}),
        wait.untilVisible(function () {return S('.organisaatio-selection')}),
        OrganisaatioHaku(function () {return S('.organisaatio')}).open,
      )
    },
    organisaatioHakuNäkyvissä: function () {
      return isElementVisible(S('.organisaatio-haku'))
    },
    valitseOrganisaatio: function (nimi) {
      return seq(
        OrganisaatioHaku(function () {return S('.organisaatio')}).select(nimi),
      )
    },
    syötäPäivämäärä: function (paivamaara) {
      return pageApi.setInputValue(esiopetuksenTarkistusraportinElementti + ' > .parametri > .calendar-input > #date-input', paivamaara)
    },
    luoRaportti: function () {
      return seq(
        wait.until(function() {
          return S(esiopetuksenTarkistusraportinElementti + ' > h2 > span').text() === 'Esiopetuksen opiskeluoikeus- ja suoritustietojen tarkistusraportti'
        }),
        click(esiopetuksenTarkistusraportinElementti + ' > .raportti-download-button > .koski-button'),
        wait.until(function() {
          return S(esiopetuksenTarkistusraportinElementti +  ' > .raportti-download-button > .ajax-indicator-bg').length === 1
        }, 10000),
        wait.until(function() {
          return S(esiopetuksenTarkistusraportinElementti + ' > .raportti-download-button > .ajax-indicator-bg').length === 0
        }, 10000)
      )
    },
    latausIndikaatioPoistuu: function () {
      return S(esiopetuksenTarkistusraportinElementti + ' > .raportti-download-button > .ajax-indicator-bg').length === 0
    }
  }
  return api
}
