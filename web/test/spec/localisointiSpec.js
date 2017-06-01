describe('Lokalisointi', function() {
  var page = KoskiPage()
  describe('Ruotsinkielisellä käyttäjällä', function() {
    before(Authentication().login('pärre'), resetFixtures, page.openPage)
    describe('Oppijataulukko', function() {
      it('Näyttää ruotsinkielisen tekstin, mikäli käännös löytyy', function() {
        expect(S('#logo').text()).to.equal('Studieinfo.fi')
      })
      it('Näyttää suomenkielisen tekstin, mikäli käännös puuttuu', function() {
        expect(S('.oppija-haku h3').text()).to.equal('Hae tai lisää opiskelija')
      })
    })
  })
  describe('Tekstien muokkaus', function() {
    function editLink() { return S('#topbar .edit-localizations') }
    function startEdit() { triggerEvent(editLink(), 'click') }
    function saveEdits() {
      triggerEvent(findSingle('.localization-edit-bar button:not(:disabled)'), 'click')
      return wait.forAjax()
    }
    function cancelEdits() { triggerEvent(findSingle('.localization-edit-bar .cancel'), 'click') }
    function selectLanguage(lang) {
      return function() {
        triggerEvent(S('.localization-edit-bar .languages .' + lang), 'click')
        var selector = '.localization-edit-bar .languages .' + lang + '.selected'
        return wait.until(function() {
          return S(selector).is(':visible')
        })()
      }
    }
    function changeText(selector, value) { return function() {
      var el = findSingle(selector + ' .editing')
      el[0].textContent = value
      triggerEvent(el, 'input')
    }}

    describe('Tavallisella käyttäjällä', function() {
      before(Authentication().login(), resetFixtures, page.openPage)
      it('Ei näytetä', function() {
        expect(editLink().is(':visible')).to.equal(false)
      })
    })

    describe('Käyttäjällä, jolla on lokalisoinnin CRUD-oikeudet', function() {
      before(Authentication().login('pää'), resetFixtures, page.openPage)

      it('Näytetään muokkauslinkki', function() {
        expect(editLink().is(':visible')).to.equal(true)
      })

      describe('Muokattaessa ruotsinkielisiä tekstejä', function() {
        before(startEdit, selectLanguage('sv'), changeText('.oppija-haku h3', 'Hae juttuja'), saveEdits)

        it('Muokattu teksti näytetään', function() {
          expect(S('.oppija-haku h3').text()).to.equal('Hae juttuja')
        })

        describe('Vaihdettaessa takaisin suomen kieleen', function() {
          before(startEdit, selectLanguage('fi'), cancelEdits)

          it('Suomenkielinen teksti näytetään', function() {
            expect(S('.oppija-haku h3').text()).to.equal('Hae tai lisää opiskelija')
          })

          describe('Vaihdettaessa vielä takaisin ruotsin kieleen', function() {
            before(startEdit, selectLanguage('sv'), cancelEdits)
            
            it('Muokattu teksti näytetään', function() {
              expect(S('.oppija-haku h3').text()).to.equal('Hae juttuja')
            })
          })
        })
      })
    })
  })
})