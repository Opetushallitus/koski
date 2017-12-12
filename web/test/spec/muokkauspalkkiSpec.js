describe('Muokkauspalkki', function () {
  function currentURL() {
    return testFrame().location.href
  }

  function editBarVisible() {
    return S('#edit-bar').hasClass('visible')
  }

  var page = KoskiPage()
  describe('Näkyvyys', function () {
    beforeEach(Authentication().login())

    before(page.openPage, page.oppijaHaku.searchAndSelect('020655-2479'))

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
          return currentURL().endsWith('/koski/virkailija') && S('#topbar + div').hasClass('oppijataulukko')
        }),
        goBack,
        wait.until(function () {
          return !currentURL().endsWith('/koski/')
        })
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
          return currentURL().endsWith('/koski/virkailija') && S('#topbar + div').hasClass('oppijataulukko')
        }),
        goBack,
        wait.until(function () {
          return !currentURL().endsWith('/koski/')
        })
      )

      it('Pysyy näkyvillä', function () {
        expect(editBarVisible()).to.equal(true)
      })
    })

  })
})
