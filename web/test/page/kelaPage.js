import {
  click,
  extractAsText,
  isElementVisible,
  isLoading,
  openPage,
  S,
  testFrame,
  wait
} from '../util/testHelpers.js'
import { Page } from './pageApi.js'

export function KelaPage() {
  let pageApi = Page(function () {
    return S('#content')
  })

  const api = {
    openPage: function () {
      return openPage('/koski/kela', api.isReady)()
    },
    openVirkailijaPage: function () {
      return openPage('/koski/virkailija')
    },
    getCurrentUrl: function () {
      return testFrame().location.href
    },
    isVisible: function () {
      return isElementVisible(S('#content .kela'))
    },
    isReady: function () {
      return api.isVisible() && !isLoading()
    },
    getOppijanNimi: function () {
      return S('h2.henkilotiedot').text()
    },
    getValittuOpiskeluoikeusOtsikko: function () {
      return S('h3.otsikko').text()
    },
    setSearchInputValue: function (value) {
      return function () {
        return pageApi.setInputValue('#kela-search-query', value)()
      }
    },
    searchAndSelect: function (hetu) {
      return function () {
        return api
          .setSearchInputValue(hetu)()
          .then(wait.forAjax)
          .then(
            wait.until(function () {
              return isElementVisible(S('.opiskeluoikeus-tabs'))
            })
          )
      }
    },
    selectOpiskeluoikeusByTyyppi: function (opiskeluoikeudenTyyppi) {
      return function () {
        let opiskeluoikeudet = S('.opiskeluoikeus-tabs > ul > li').toArray()
        let opiskeluoikeus = opiskeluoikeudet.find(function (li) {
          return $(li).text().includes(opiskeluoikeudenTyyppi)
        })
        return click(opiskeluoikeus)()
      }
    },
    selectSuoritus: function (suoritusTabNimi) {
      return function () {
        let suoritukset = S('.suoritukset > .tabs > ul > li').toArray()
        let suoritus = suoritukset.find(function (li) {
          return $(li).text().includes(suoritusTabNimi)
        })
        return click(suoritus)()
      }
    },
    selectOsasuoritus: function (osasuoritukseNimi) {
      return function () {
        let osasuoritukset = S(
          'tr.osasuoritukset > td > span.suorituksen-nimi'
        ).toArray()
        let osasuoritus = osasuoritukset.find(function (li) {
          return $(li).text().includes(osasuoritukseNimi)
        })
        return click(osasuoritus)()
      }
    },
    selectFromVersiohistoria: function (versionumero) {
      return function () {
        let versiot = S(
          '.versiohistoria > .kela-modal > .kela-modal-content > ol > li > a'
        ).toArray()
        let versio = versiot.find(function (li) {
          return $(li).text().startsWith(versionumero)
        })
        return click(versio)()
      }
    },
    openVersiohistoria: function () {
      return click(S('.versiohistoria > span'))()
    },
    getValittuVersioVersiohistoriasta: function () {
      return extractAsText(S('.kela-modal-content > ol > li.selected'))
    },
    palaaVersiohistoriastaLinkkiIsVisible: function () {
      return (
        extractAsText(S('.palaa-versiohistoriasta')) ===
        'Palaa versiohistoriasta yleisnäkymään'
      )
    },
    clickPalaaVersiohistoriasta: function () {
      return click(S('.palaa-versiohistoriasta > a'))()
    }
  }
  return api
}
