function RaportitPage() {

  const tabsElementti = 'div.main-content .tabs-container'
  const raporttivalitsinElementti = 'div.main-content .raportti-valitsin .pills-container'

  var api = {
    openPage: function (predicate) {
      return function () {
        return openPage('/koski/raportit?tilastoraportit=true', predicate)()
      }
    },
    odotaRaporttikategoriat: function () {
      return wait.untilVisible(() => S(tabsElementti))
    },
    raporttikategoriat: function() {
      return getAsTextArray(`${tabsElementti} .tabs-item-text`)
    },
    valittuRaporttikategoria: function() {
      return S(`${tabsElementti} .tabs-item-selected .tabs-item-text`).text()
    },
    valitseRaporttikategoria: function(index) {
      return wait.until(() => S(`${tabsElementti} .tabs-item:nth-child(${index + 1})`).click())
    },
    otsikko: function() {
      return S('div.main-content > h2').text()
    },
    raportit: function(raportit) {
      return getAsTextArray(`${raporttivalitsinElementti} .pills-item`)
    },
    valittuRaportti: function() {
      return S(`${raporttivalitsinElementti} .pills-item-selected`).text()
    },
    valitseRaportti: function(index) {
      return wait.until(() => S(`${raporttivalitsinElementti} .pills-item:nth-child(${index + 1})`).click())
    },
    organisaatioValitsinNäkyvillä: function() {
      return isElementVisible(S('.organisaatio-dropdown'))
    },
    valitseOrganisaatio: function(index) {
      return seq(
        () => S('.organisaatio-dropdown input').click(),
        wait.untilVisible(() => S('.organisaatio-dropdown .options.open')),
        () => S(`.organisaatio-dropdown .options .option:nth-child(${index + 1}) .value`).click(),
        wait.untilHidden(() => S('.organisaatio-dropdown .options.open'))
      )
    },
    haeOrganisaatioita: function(hakusana) {
      return function() {
        setInputValue('.organisaatio-dropdown input', hakusana)
      }
    },
    valittuOrganisaatio: function() {
      return S('.organisaatio-dropdown .input-container input').val()
    },
    valittavatOrganisaatiot: function() {
      return getAsTextArray('.organisaatio-dropdown .options .option')
    },
    valitutPäivät: function() {
      return getValuesAsArray('.date-editor')
    },
    latausnappiAktiivinen: function() {
      return !S('.raportti-download-button button').prop('disabled')
    },
    syötäAika: function(inputIndex, aika) {
      return function() {
        setInputValue(S('.date-editor')[inputIndex], aika)
      }
    }
  }
  return api
}
