import { Authentication } from '../page/authentication.js'
import { LandingPage, KorhoPankki } from '../page/landingPage.js'
import { OmatTiedotPage } from '../page/omatTiedotPage.js'
import { OpinnotPage } from '../page/opinnotPage.js'
import { expect } from '../util/chai.esm.js'
import {
  wait,
  click,
  extractAsText,
  S,
  resetFixtures,
  findSingle
} from '../util/testHelpers.js'

describe('Suostumuksen peruutus', function () {
  let opinnot = OpinnotPage()
  let etusivu = LandingPage()
  let korhopankki = KorhoPankki()
  let omattiedot = OmatTiedotPage()
  let authentication = Authentication()

  describe('Koulutuksen suostumuksen voi perua', function () {
    before(
      authentication.login(),
      resetFixtures,
      authentication.logout,
      etusivu.openPage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('010917-156A'),
      wait.until(omattiedot.isVisible),
      opinnot.valitseOmatTiedotOpiskeluoikeus(
        'Vapaan sivistystyön koulutus (2022—2022, hyväksytysti suoritettu)'
      )
    )

    it('Tämän opiskeluoikeuden tiedot näytetään.. -teksti näkyy', function () {
      expect(extractAsText(S('.suostumuksen-peruuttaminen'))).to.equal(
        'Tämän opiskeluoikeuden tiedot näytetään antamasi suostumuksen perusteella. Peruuta suostumus'
      )
    })

    describe('Infoboxin teksti', function () {
      before(click('.info-icon'))

      it('Näkyy', function () {
        expect(extractAsText(S('.suostumuksen-perumisen-info'))).to.include(
          'Tämän koulutuksen tiedot on tallennettu Opintopolkuun'
        )
      })

      after(click('.info-icon'))
    })

    describe('Perutaan suostumus', function () {
      before(click('.peru-suostumus-linkki'))

      it('Suostumusta ei voi perua ilman checkboxin merkkaamista', function () {
        expect(findSingle('.vahvista')()[0].disabled).to.equal(true)
      })

      describe('Vahvistetaan ja perutaan suostumus', function () {
        before(
          click('#suostumuksen-peruutus-checkbox'),
          click('.vahvista'),
          wait.forMilliseconds(200) // page reloads
        )

        it('Koulutus on poistettu', function () {
          expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([])
        })
      })
    })
  })

  describe('Koulutuksen suostumusta ei voi perua', function () {
    describe('Kun opiskeluoikeus ei ole peruttavaa tyyppiä', function () {
      before(
        authentication.login(),
        resetFixtures,
        authentication.logout,
        etusivu.openPage,
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('231158-467R'),
        wait.until(omattiedot.isVisible),
        opinnot.valitseOmatTiedotOpiskeluoikeus(
          'Lukutaitokoulutus oppivelvollisille (2021—, läsnä)'
        )
      )

      it('Suostumuksen perumisen elementti puuttuu', function () {
        expect(S('.suostumuksen-perumisen-info').length).to.equal(0)
      })
    })

    describe('Kun opiskeluoikeudesta on tehty suoritusjako', function () {
      let form = omattiedot.suoritusjakoForm

      before(
        authentication.login(),
        resetFixtures,
        authentication.logout,
        etusivu.openPage,
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('010917-156A'),
        wait.until(omattiedot.isVisible),
        click(omattiedot.suoritusjakoButton),
        click('.koski-checkbox__input'),
        form.createSuoritusjako(),
        wait.until(form.suoritusjako(1).isVisible),
        opinnot.valitseOmatTiedotOpiskeluoikeus(
          'Vapaan sivistystyön koulutus (2022—2022, hyväksytysti suoritettu)'
        )
      )

      it('Suostumuksen perumisen linkki puuttuu', function () {
        expect(extractAsText(S('.suostumuksen-peruuttaminen'))).to.equal(
          'Tämän opiskeluoikeuden tiedot näytetään antamasi suostumuksen perusteella.'
        )
      })
    })
  })
})
