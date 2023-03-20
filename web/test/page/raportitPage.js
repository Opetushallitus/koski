import {
  getAsTextArray,
  getValuesAsArray,
  isElementVisible,
  openPage,
  S,
  seq,
  setInputValue,
  wait
} from '../util/testHelpers.js'

export function RaportitPage() {
  const tabsElementti = 'div.main-content .tabs-container'
  const raporttivalitsinElementti =
    'div.main-content .raportti-valitsin .pills-container'

  const api = {
    openPage: function (predicate) {
      return function () {
        return openPage('/koski/raportit?tilastoraportit=true', predicate)()
      }
    },
    lataaRaportointikanta: function () {
      return Q(
        $.ajax({ url: '/koski/api/test/raportointikanta/load', method: 'get' })
      )
    },
    odotaRaportointikantaOnLatautunut: function () {
      return api.checkRaportointikantaStatus(30)
    },
    checkRaportointikantaStatus: function (retriesLeft) {
      return api
        .raportointikantaStatus()
        .then(function () {})
        .catch(function (err) {
          if (err.status === 503) {
            if (retriesLeft > 0) {
              return wait
                .forMilliseconds(1000)()
                .then(function () {
                  return api.checkRaportointikantaStatus(retriesLeft - 1)
                })
            } else {
              throw Error('Timeout: Raportointikanta ei ole latautunut')
            }
          } else {
            throw err
          }
        })
    },
    raportointikantaStatus: function () {
      return $.ajax({ url: '/koski/api/raportit/paivitysaika', method: 'get' })
    },
    odotaRaporttikategoriat: function () {
      return wait.untilVisible(() => S(tabsElementti))
    },
    raporttikategoriat: function () {
      return getAsTextArray(`${tabsElementti} .tabs-item-text`)
    },
    valittuRaporttikategoria: function () {
      return S(`${tabsElementti} .tabs-item-selected .tabs-item-text`).text()
    },
    valitseRaporttikategoria: function (index) {
      return wait.until(() =>
        S(`${tabsElementti} .tabs-item:nth-child(${index + 1})`).click()
      )
    },
    otsikko: function () {
      return S('div.main-content > h2').text()
    },
    raportit: function (_raportit) {
      return getAsTextArray(`${raporttivalitsinElementti} .pills-item`)
    },
    valittuRaportti: function () {
      return S(`${raporttivalitsinElementti} .pills-item-selected`).text()
    },
    valitseRaportti: function (index) {
      return wait.until(() =>
        S(
          `${raporttivalitsinElementti} .pills-item:nth-child(${index + 1})`
        ).click()
      )
    },
    organisaatioValitsinNäkyvillä: function () {
      return isElementVisible(S('.organisaatio-dropdown'))
    },
    valitseOrganisaatio: function (index) {
      return seq(
        () => S('.organisaatio-dropdown input').click(),
        wait.untilVisible(() => S('.organisaatio-dropdown .options.open')),
        () =>
          S(
            `.organisaatio-dropdown .options .option:nth-child(${
              index + 1
            }) .value`
          ).click(),
        wait.untilHidden(() => S('.organisaatio-dropdown .options.open'))
      )
    },
    haeOrganisaatioita: function (hakusana) {
      return function () {
        setInputValue('.organisaatio-dropdown input', hakusana)
      }
    },
    valittuOrganisaatio: function () {
      return S('.organisaatio-dropdown .input-container input').val()
    },
    valittavatOrganisaatiot: function () {
      return getAsTextArray('.organisaatio-dropdown .options > .option .value')
    },
    valitutPäivät: function () {
      return getValuesAsArray('.date-editor')
    },
    latausnappiAktiivinen: function () {
      return !S('.raportti-download-button button').prop('disabled')
    },
    syötäAika: function (inputIndex, aika) {
      return function () {
        setInputValue(S('.date-editor')[inputIndex], aika)
      }
    },
    raportinPäivitysaika: function () {
      return S('.update-time .datetime').text()
    }
  }
  return api
}
