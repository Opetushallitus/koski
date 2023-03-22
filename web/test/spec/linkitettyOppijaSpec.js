import { Authentication } from '../page/authentication.js'
import { KoskiPage } from '../page/koskiPage.js'
import { OpinnotPage } from '../page/opinnotPage.js'
import { expect } from '../util/chai.esm.js'
import { resetFixtures, S, timeout, wait } from '../util/testHelpers.js'

describe('Linkitetyt oppijat', function () {
  let page = KoskiPage()
  let opinnot = OpinnotPage()
  let editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures, page.openPage)
  describe('Kun katsotaan master-henkilön tietoja', function () {
    this.timeout(20000)
    before(timeout.overrideWaitTime(20000))
    after(timeout.resetDefaultWaitTime())

    before(page.oppijaHaku.searchAndSelect('Master'))
    describe('Tietojen katsominen', function () {
      it('Näytetään myös slave-henkilön opiskeluoikeudet', function () {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeustyypit()).to.have.members(
          ['Lukiokoulutus', 'Perusopetus']
        )
      })
    })
    describe('Slaveen liitettyjen tietojen muuttaminen', function () {
      before(
        opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('lukiokoulutus'),
        editor.edit,
        editor
          .property('oppimäärä')
          .setValue('Lukio suoritetaan aikuisten opetussuunnitelman mukaan'),
        editor.saveChanges
      )

      it('Tallennus onnistuu', function () {})
    })

    describe('Versiohistorian katsominen', function () {
      let versiohistoria = opinnot.versiohistoria

      before(versiohistoria.avaa, versiohistoria.valitse('v1'))

      it('Toimii', function () {})
    })
  })

  describe('Kun haetaan slave-henkilön oidilla', function () {
    before(
      page.oppijaHaku.searchAndSelect('1.2.246.562.24.00000051473', 'Master')
    )
    describe('Tietojen katsominen', function () {
      it('Näytetään master-henkilön tiedot', function () {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeustyypit()).to.have.members(
          ['Lukiokoulutus', 'Perusopetus']
        )
      })
    })
  })

  describe('Uuden oppijan lisääminen slave-henkilön oidilla', function () {
    before(function () {
      return page.oppijaHaku
        .search(
          '1.2.246.562.24.41000051473',
          page.oppijaHaku.isNoResultsLabelShown
        )()
        .then(wait.until(page.oppijaHaku.canAddNewOppija))
        .then(page.oppijaHaku.addNewOppija)
    })

    describe('Näytetään', function () {
      it('slave-henkilön tiedot', function () {
        expect(S('.uusi-oppija .etunimet input').val()).to.equal('Slave')
      })
    })
  })

  describe('Slave-henkilöön liitetyt opiskeluoikeudet oppijataulukossa', function () {
    before(page.openPage, page.oppijataulukko.filterBy('nimi', 'Master'))

    it('Näytetään master-henkilön tiedot', function () {
      expect(page.oppijataulukko.names()).to.deep.equal([
        'of Puppets, Master',
        'of Puppets, Master'
      ])
    })
  })

  describe('Oppijahaussa', function () {
    before(page.oppijaHaku.search('Puppets'))

    it('Näytetään vain master-henkilön tiedot', function () {
      expect(page.oppijaHaku.getSearchResults()).to.deep.equal([
        'of Puppets, Master (101097-6107)'
      ])
    })
  })
})
