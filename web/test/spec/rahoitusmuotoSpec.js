import { AddOppijaPage } from '../page/addOppijaPage.js'
import { Authentication } from '../page/authentication.js'
import { KoskiPage, prepareForNewOppija } from '../page/koskiPage.js'
import { OpinnotPage, OpiskeluoikeusDialog } from '../page/opinnotPage.js'
import { expect } from '../util/chai.esm.js'
import { extractAsText, S, resetFixtures } from '../util/testHelpers.js'

describe('Rahoitusmuoto', function () {
  let page = KoskiPage()
  let opinnot = OpinnotPage()
  let editor = opinnot.opiskeluoikeusEditor()
  let addOppija = AddOppijaPage()

  before(Authentication().login(), resetFixtures)

  describe('Uuden opiskeluoikeuden lisääminen', function () {
    describe('Kun lisätään läsnä-tilainen opiskeluoikeus ja ei valita rahoitusmuotoa', function () {
      before(
        prepareForNewOppija('kalle', '020782-5339'),
        addOppija.enterValidDataDIAValmistavaVaihe({
          etunimet: 'Doris',
          kutsumanimi: 'Doris',
          sukunimi: 'Dia'
        }),
        addOppija.submitAndExpectSuccess(
          'Dia, Doris (020782-5339)',
          'Valmistava DIA-vaihe'
        )
      )

      it('Rahoitusmuoto on täydennetty valmiiksi', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.include(
          'Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })
    })

    describe('Kun lisätänää läsnä-tilainen opiskeluoikeus ja valitaan rahoitusmuoto', function () {
      before(
        prepareForNewOppija('kalle', '020782-5339'),
        addOppija.enterValidDataDIAValmistavaVaihe({
          opintojenRahoitus: 'Muuta kautta rahoitettu',
          etunimet: 'Doris',
          kutsumanimi: 'Doris',
          sukunimi: 'Dia'
        }),
        addOppija.submitAndExpectSuccess(
          'Dia, Doris (020782-5339)',
          'Valmistava DIA-vaihe'
        )
      )

      it('Rahoitusmuoto on oikea', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.include(
          'Läsnä (muuta kautta rahoitettu)'
        )
      })
    })
  })

  describe('Tilan lisäämien opiskeluoikeudelle', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('200300-624E'),
      editor.edit
    )

    describe('Jos rahoitusmuoto on valittu, ja valitaan tila jolle ei voi valita rahoitusmuotoa, rahoitusmuoto tyhjennetään', function () {
      before(
        opinnot.avaaLisaysDialogi,
        OpiskeluoikeusDialog().tila().aseta('valmistunut'),
        OpiskeluoikeusDialog().opintojenRahoitus().aseta('6'),
        OpiskeluoikeusDialog().tila().aseta('valiaikaisestikeskeytynyt'),
        OpiskeluoikeusDialog().tallenna
      )

      it('Loma tilalle ei ole valittu rahoitusmuotoa', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.contain(
          'Väliaikaisesti keskeytynyt\n'
        )
      })

      describe('Valitaan valmistunut-tila', function () {
        before(
          opinnot.avaaLisaysDialogi,
          OpiskeluoikeusDialog().tila().aseta('valmistunut'),
          OpiskeluoikeusDialog().tallenna
        )

        it('Valmistunut tilalle on valittu automaattisesti rahoitusmuoto', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.contain(
            'Valmistunut (valtionosuusrahoitteinen koulutus)\n'
          )
        })
      })
    })
  })
})
