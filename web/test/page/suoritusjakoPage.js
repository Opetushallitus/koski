function SuoritusjakoPage() {
  var api = {
    openPage: function (secretKey, jakoTyyppi) {
      return function () {
        openPage(
          '/koski/opinnot/' +
            (jakoTyyppi ? jakoTyyppi + '/' : '') +
            window.secrets[secretKey],
          api.isVisible
        )()
      }
    },
    changeLanguageButtonFinnish: function () {
      return S('#topbar #change-lang-fi')
    },
    changeLanguageButtonSwedish: function () {
      return S('#topbar #change-lang-sv')
    },
    changeLanguageButtonEnglish: function () {
      return S('#topbar #change-lang-en')
    },
    header: function () {
      return S('.suoritusjako-page .oppija header')
    },
    headerText: function () {
      return S('.suoritusjako-page .oppija header').text()
    },
    isVisible: function () {
      return isElementVisible(S('.suoritusjako-page')) && !isLoading()
    },
    oppilaitosTitleText: function () {
      return textsOf(
        S('.oppilaitos-list .oppilaitos-container h2.oppilaitos-title')
      )
    },
    opiskeluoikeusTitleText: function () {
      return textsOf(
        S(
          '.oppilaitos-list .oppilaitos-container .opiskeluoikeudet-list button'
        )
      )
    },
    avaaOpiskeluoikeus: function (teksti) {
      // Opiskeluoikeuslista renderöityy vasta kun oppijan tiedot on haettu.
      // omattiedot.isVisible ei takaa sitä: se katsoo vain että .omattiedot on
      // näkyvissä eikä .loading-elementtiä ole, ja haun ja sen latausindikaat-
      // torin väliin jää ikkuna jossa ehto on tosi mutta nappeja ei vielä ole.
      // Odotetaan siis nappia itseään, ei kiinteää millisekuntimäärää.
      var selector =
        '.oppilaitos-list .oppilaitos-container .opiskeluoikeudet-list button.opiskeluoikeus-button:contains(' +
        teksti +
        ')'
      return function () {
        return wait
          .until(function () {
            return S(selector).length > 0
          })()
          .then(click(findSingle(selector)))
      }
    }
  }
  return api
}
