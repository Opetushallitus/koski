describe('Muokkauspalkki', function () {
  function currentURL() {
    return testFrame().location.href
  }

  function editBarVisible() {
    return S('#edit-bar').hasClass('visible')
  }

  function click(selector) {
    triggerEvent(S(selector)[0], 'click')
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
      before(function () {
        click('button.toggle-edit')
      })
      it('Näkyvillä', function () {
        expect(editBarVisible()).to.equal(true)
      })
    })

    describe('Muokkaustilasta poistuttaessa', function () {
      before(function () {
        click('a.cancel')
      })
      it('Piilossa', function () {
        expect(editBarVisible()).to.equal(false)
      })
    })


    describe('Oppijataulukosta näyttötilaan edellinen-painikkeella palattaessa', function () {
      before(function () {
        this.timeout(2000)
        click('a.back-link')
        wait.until(function () {
          return currentURL().endsWith('/koski/') && S('#topbar + div').hasClass(':oppijataulukko')
        })
        goBack()
        wait.until(function () {
          return !currentURL().endsWith('/koski/')
        })
      })
      it('Pysyy piilossa', function () {
        expect(editBarVisible()).to.equal(false)
      })
    })

    describe('Oppijataulukosta näyttötilaan edellinen-painikkeella palattaessa', function () {
      before(function () {
        this.timeout(2000)
        click('button.toggle-edit')
        click('a.back-link')
        wait.until(function () {
          return currentURL().endsWith('/koski/') && S('#topbar + div').hasClass(':oppijataulukko')
        })
        goBack()
        wait.until(function () {
          return !currentURL().endsWith('/koski/')
        })
      })

      it('Pysyy näkyvillä', function () {
        expect(editBarVisible()).to.equal(true)
      })
    })

  })
})
