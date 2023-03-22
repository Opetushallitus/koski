import { AddOppijaPage } from '../page/addOppijaPage.js'
import { Authentication } from '../page/authentication.js'
import { KoskiPage } from '../page/koskiPage.js'
import { OpinnotPage } from '../page/opinnotPage.js'
import { expect } from '../util/chai.esm.js'
import { wait, S, resetFixtures } from '../util/testHelpers.js'

describe('Opiskeluoikeuden sisältyvyys', function () {
  let addOppija = AddOppijaPage()
  let page = KoskiPage()
  let opinnot = OpinnotPage()
  let editor = opinnot.opiskeluoikeusEditor()

  let sisältävänOpiskeluoikeudenId

  before(
    Authentication().login(),
    resetFixtures,
    page.openPage,
    page.oppijaHaku.searchAndSelect('280618-402H'),
    function () {
      sisältävänOpiskeluoikeudenId = S('.opiskeluoikeus .id .value').text()
    }
  )

  describe('Sisältyvän opiskeluoikeuden luominen', function () {
    before(
      Authentication().login('omnia-tallentaja'),
      page.openPage,
      page.oppijaHaku.search('280618-402H', page.oppijaHaku.canAddNewOppija),
      page.oppijaHaku.addNewOppija,
      addOppija.enterValidDataAmmatillinen({ oppilaitos: 'Omnia' }),
      addOppija.submitAndExpectSuccess('280618-402H', 'Autoalan perustutkinto'),
      editor.edit,
      editor.property('sisältyyOpiskeluoikeuteen').addValue,
      editor
        .property('oppilaitos')
        .organisaatioValitsin()
        .select('Stadin ammatti- ja aikuisopisto')
    )

    describe('Kun oppilaitos on tyyppiä oppisopimustoimisto', function () {
      before(
        editor
          .property('oppilaitos')
          .organisaatioValitsin()
          .select('Stadin oppisopimuskeskus')
      )

      it('se voidaan voidaan valita', function () {})

      after(
        editor
          .property('oppilaitos')
          .organisaatioValitsin()
          .select('Stadin ammatti- ja aikuisopisto')
      )
    })

    describe('Kun sisältävää opiskeluoikeutta ei löydy id:llä', function () {
      before(
        editor
          .property('sisältyyOpiskeluoikeuteen')
          .property('oid')
          .setValue(929292929),
        editor.saveChangesAndExpectError,
        wait.until(page.isErrorShown)
      )
      it('Tallennus epäonnistuu ja näytetään virheilmoitus', function () {
        expect(page.getErrorMessage()).to.equal(
          'Sisältävää opiskeluoikeutta ei löydy oid-arvolla 929292929'
        )
      })
    })

    describe('Kun sisältävä opiskeluoikeus löytyy oid:llä', function () {
      before(function () {
        return editor
          .property('sisältyyOpiskeluoikeuteen')
          .property('oid')
          .setValue(sisältävänOpiskeluoikeudenId)()
      }, editor.saveChanges)

      it('tallennus onnistuu', function () {})

      describe('Sisältyvän opiskeluoikeuden näkyvyys sisältävän opiskeluoikeuden organisaatiolle', function () {
        before(
          Authentication().login('tallentaja'),
          page.openPage,
          page.oppijaHaku.searchAndSelect('280618-402H')
        )

        it('Lukuoikeudet on', function () {
          expect(
            opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
          ).to.have.members([
            'Stadin ammatti- ja aikuisopisto, Luonto- ja ympäristöalan perustutkinto (2012—2016, valmistunut)',
            'Omnia, Autoalan perustutkinto (2018—, läsnä)'
          ])
        })

        it('Kirjoitusoikeuksia ei ole', function () {
          expect(opinnot.opiskeluoikeusEditor('Stadin').isEditable()).to.equal(
            true
          )
          expect(opinnot.opiskeluoikeusEditor('Omnia').isEditable()).to.equal(
            false
          )
        })
      })
    })
  })
})
