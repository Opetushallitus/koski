import {
  click,
  isElementVisible,
  openPage,
  S,
  seq,
  wait
} from '../util/testHelpers.js'
import { Page } from './pageApi.js'

export function TiedonsiirrotPage() {
  function poistaNappi() {
    return S('button.remove-selected')
  }

  const api = {
    openPage: function () {
      return openPage('/koski/tiedonsiirrot', api.isVisible)()
    },
    isVisible: function () {
      return isElementVisible(
        S('#content .tiedonsiirrot-content .tiedonsiirto-taulukko')
      )
    },
    tiedot: function () {
      return S('.tiedonsiirrot-content table tbody tr')
        .toArray()
        .map(function (row) {
          return $(row)
            .find('td:not(.tila):not(.aika):not(.valitse)')
            .toArray()
            .map(function (td) {
              return $(td).text().trim()
            })
        })
    },
    poista: click('button.remove-selected'),
    poistaNappiNäkyvissä: function () {
      return isElementVisible(poistaNappi())
    },
    poistaNappiEnabloitu: function () {
      return poistaNappi().is(':visible') && poistaNappi().is(':enabled')
    },
    rivinValintaNäkyvissä: function () {
      return isElementVisible(S('.tiedonsiirto-taulukko .valitse'))
    },
    setValinta: function (id, selected) {
      return Page().setInputValue(
        '#' + id.replace(/(\.)/g, '\\$1') + ' input',
        selected
      )
    },
    setValintaViimeiseen: function (selected) {
      return Page().setInputValue(
        '.tiedonsiirrot tbody:last-child input',
        selected
      )
    },
    openVirhesivu: seq(
      click('.virheet-link'),
      wait.untilVisible('#content .tiedonsiirto-virheet')
    ),
    openYhteenveto: seq(
      click('.yhteenveto-link'),
      wait.untilVisible('#content .tiedonsiirto-yhteenveto'),
      wait.forAjax
    )
  }
  return api
}
