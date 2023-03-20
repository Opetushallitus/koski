import { Authentication } from '../page/authentication.js'
import { KoskiPage } from '../page/koskiPage.js'
import { OpinnotPage } from '../page/opinnotPage.js'
import { expect } from '../util/chai.esm.js'
import {
  wait,
  click,
  S,
  resetFixtures,
  findSingle,
  triggerEvent
} from '../util/testHelpers.js'

describe('Lokalisointi', function () {
  let page = KoskiPage()
  let opinnot = OpinnotPage()
  let editor = opinnot.opiskeluoikeusEditor()

  describe('Ruotsinkielisellä käyttäjällä', function () {
    before(Authentication().login('pärre'), resetFixtures, page.openPage)
    describe('Oppijataulukko', function () {
      it('Näyttää ruotsinkielisen tekstin, mikäli käännös löytyy', function () {
        expect(S('.oppija-haku h3').text()).to.equal(
          'Sök eller lägg till studerande'
        )
      })
      it('Näyttää suomenkielisen tekstin, mikäli käännös puuttuu', function () {
        expect(S('#logo').text()).to.equal('Opintopolku.fi')
      })
    })
  })
  describe('Sovelluksen käyttöliittymätekstien muokkaus', function () {
    function editLink() {
      return S('.edit-localizations')
    }
    let startEdit = click(editLink)
    let saveEdits = click(
      findSingle('.localization-edit-bar button:not(:disabled)')
    )
    let cancelEdits = click(findSingle('.localization-edit-bar .cancel'))

    function selectLanguage(lang) {
      return function () {
        let selector =
          '.localization-edit-bar .languages .' + lang + '.selected'
        // eslint-disable-next-line no-undef
        return Q()
          .then(click(S('.localization-edit-bar .languages .' + lang)))
          .then(wait.untilVisible(findSingle(selector)))
      }
    }
    function changeText(selector, value) {
      return function () {
        let el = findSingle(selector + ' .editing')()
        el[0].textContent = value
        return triggerEvent(el, 'input')()
      }
    }

    describe('Tavallisella käyttäjällä', function () {
      before(Authentication().login(), resetFixtures, page.openPage)
      it('Ei näytetä', function () {
        expect(editLink().is(':visible')).to.equal(false)
      })
    })

    describe('Käyttäjällä, jolla on lokalisoinnin CRUD-oikeudet', function () {
      before(Authentication().login('pää'), resetFixtures, page.openPage)

      it('Näytetään muokkauslinkki', function () {
        expect(editLink().is(':visible')).to.equal(true)
      })

      describe('Muokattaessa ruotsinkielisiä tekstejä', function () {
        before(
          startEdit,
          selectLanguage('sv'),
          changeText('.oppija-haku h3', 'Hae juttuja'),
          saveEdits
        )

        it('Muokattu teksti näytetään', function () {
          expect(S('.oppija-haku h3').text()).to.equal('Hae juttuja')
        })

        describe('Vaihdettaessa takaisin suomen kieleen', function () {
          before(startEdit, selectLanguage('fi'), cancelEdits)

          it('Suomenkielinen teksti näytetään', function () {
            expect(S('.oppija-haku h3').text()).to.equal(
              'Hae tai lisää opiskelija'
            )
          })

          describe('Vaihdettaessa vielä takaisin ruotsin kieleen', function () {
            before(startEdit, selectLanguage('sv'), cancelEdits)

            it('Muokattu teksti näytetään', function () {
              expect(S('.oppija-haku h3').text()).to.equal('Hae juttuja')
            })
          })
        })
      })
    })
  })

  describe('Ruotsinkielisen virkailijan syöttämä paikallinen oppiaine', function () {
    let oppiaineet = opinnot.oppiaineet.uusiOppiaine('.pakolliset')
    let paikallinen = editor.subEditor('.pakollinen.paikallinen')
    before(
      Authentication().login('pärre'),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('220109-784L'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus'),
      editor.edit,
      oppiaineet.selectValue('Lägg till'),
      paikallinen.propertyBySelector('.arvosana').selectValue('7'),
      paikallinen.propertyBySelector('.koodi').setValue('TNS'),
      paikallinen.propertyBySelector('.nimi').setValue('Dans'),
      editor.saveChanges,
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('220109-784L'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus')
    )

    it('Näkyy myös suomenkieliselle virkailijalle', function () {
      expect(paikallinen.propertyBySelector('.oppiaine').getText()).to.equal(
        'Dans'
      )
    })
  })

  describe('Monikieliset tekstit muokattavassa datassa', function () {
    let property = editor.property('todistuksellaNäkyvätLisätiedot')

    describe('Syötettäessä suomenkielisellä käyttäjällä', function () {
      before(
        Authentication().login(),
        resetFixtures,
        page.openPage,
        page.oppijaHaku.searchAndSelect('220109-784L'),
        editor.edit,
        property.setValue('Hyvä meininki'),
        editor.saveChanges
      )
      it('Teksti merkitään suomenkieliseksi', function () {
        expect(property.getLanguage()).to.equal('fi')
      })

      describe('Muokattaessa suomenkielistä tekstiä ruotsinkielisen käyttäjän toimesta', function () {
        before(
          Authentication().login('pärre'),
          page.openPage,
          page.oppijaHaku.searchAndSelect('220109-784L')
        )

        it('Ennen muokkausta näytetään suomenkielinen teksti', function () {
          expect(property.getLanguage()).to.equal('fi')
        })

        describe('Muokkauksen jälkeen', function () {
          before(
            editor.edit,
            property.setValue('Erittäin hyvä meininki'),
            editor.saveChanges
          )

          it('Teksti säilyy suomenkielisenä', function () {
            expect(property.getValue()).to.equal('Erittäin hyvä meininki')
            expect(property.getLanguage()).to.equal('fi')
          })

          describe('Kun poistetaan teksti, tallennetaan ja muokataan uudelleen', function () {
            before(
              editor.edit,
              property.setValue(''),
              editor.saveChanges,
              editor.edit,
              property.setValue('Jättebra'),
              editor.saveChanges
            )

            it('Uusi teksti merkitään ruotsinkieliseksi', function () {
              expect(property.getValue()).to.equal('Jättebra')
              expect(property.getLanguage()).to.equal('sv')
            })

            describe('Muokattaessa ruotsinkielistä tekstiä suomenkielisen käyttäjän toimesta', function () {
              before(
                Authentication().login('kalle'),
                page.openPage,
                page.oppijaHaku.searchAndSelect('220109-784L'),
                editor.edit,
                property.setValue('Jättebra!!'),
                editor.saveChanges
              )

              it('Teksti säilyy ruotsinkielisenä', function () {
                expect(property.getValue()).to.equal('Jättebra!!')
                expect(property.getLanguage()).to.equal('sv')
              })
            })
          })
        })
      })
    })
  })
})
