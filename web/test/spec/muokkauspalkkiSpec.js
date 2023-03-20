import { Authentication } from '../page/authentication.js'
import { KoskiPage } from '../page/koskiPage.js'
import { expect } from '../util/chai.esm.js'
import {
  wait,
  click,
  S,
  isElementVisible,
  testFrame,
  goBack
} from '../util/testHelpers.js'

describe('Muokkauspalkki', function () {
  function currentURL() {
    return testFrame().location.href
  }

  function editBarVisible() {
    return S('#edit-bar-wrapper').hasClass('visible')
  }

  let page = KoskiPage()
  let auth = Authentication()
  describe('Näkyvyys', function () {
    beforeEach(auth.login())

    before(
      auth.login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('020655-2479')
    )

    describe('Näyttötilassa', function () {
      it('piilossa', function () {
        expect(editBarVisible()).to.equal(false)
      })
    })

    describe('Muokkaustilassa', function () {
      before(click('button.toggle-edit'))
      it('Näkyvillä', function () {
        expect(editBarVisible()).to.equal(true)
      })
    })

    describe('Muokkaustilasta poistuttaessa', function () {
      before(click('a.cancel'))
      it('Piilossa', function () {
        expect(editBarVisible()).to.equal(false)
      })
    })

    describe('Oppijataulukosta näyttötilaan edellinen-painikkeella palattaessa', function () {
      before(
        click('a.back-link'),
        wait.until(function () {
          return (
            currentURL().endsWith('/koski/virkailija') &&
            isElementVisible('.oppijataulukko')
          )
        }),
        wait.prepareForNavigation,
        goBack,
        wait.forNavigation,
        wait.until(page.isVisible)
      )

      it('Pysyy piilossa', function () {
        expect(editBarVisible()).to.equal(false)
      })
    })

    describe('Oppijataulukosta näyttötilaan edellinen-painikkeella palattaessa', function () {
      before(
        click('button.toggle-edit'),
        click('a.back-link'),
        wait.until(function () {
          return (
            currentURL().endsWith('/koski/virkailija') &&
            isElementVisible('.oppijataulukko')
          )
        }),
        wait.prepareForNavigation,
        goBack,
        wait.forNavigation,
        wait.until(page.isVisible)
      )

      it('Pysyy näkyvillä', function () {
        expect(editBarVisible()).to.equal(true)
      })
    })
  })
})
