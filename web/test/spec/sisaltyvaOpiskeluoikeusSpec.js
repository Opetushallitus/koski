describe('Opiskeluoikeuden sisältyvyys', function() {
  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var login = LoginPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  var sisältävänOpiskeluoikeudenId;

  before(
    Authentication().login(), resetFixtures, page.openPage, page.oppijaHaku.searchAndSelect('280618-402H'),
    function() { sisältävänOpiskeluoikeudenId = S('.opiskeluoikeus .id .value').text() }
  )

  describe('Sisältyvän opiskeluoikeuden luominen', function() {
    before(
      Authentication().login('omnia-tallentaja'),
      page.openPage,
      page.oppijaHaku.search('280618-402H', page.oppijaHaku.canAddNewOppija),
      page.oppijaHaku.addNewOppija,
      addOppija.enterValidDataAmmatillinen({ oppilaitos: 'Omnian ammattiopisto'}),
      addOppija.submitAndExpectSuccess('280618-402H', 'Autoalan perustutkinto'),
      editor.edit,
      editor.property('sisältyyOpiskeluoikeuteen').addValue,
      editor.property('oppilaitos').organisaatioValitsin().select('Stadin ammattiopisto')
    )

    describe('Kun sisältävää opiskeluoikeutta ei löydy id:llä', function() {
      before(
        editor.property('sisältyyOpiskeluoikeuteen').property('oid').setValue(929292929),
        editor.saveChangesAndExpectError,
        wait.until(page.isErrorShown)
      )
      it('Tallennus epäonnistuu ja näytetään virheilmoitus', function() {
        expect(page.getErrorMessage()).to.equal('Sisältävää opiskeluoikeutta ei löydy oid-arvolla 929292929')
      })
    })

    describe('Kun sisältävä opiskeluoikeus löytyy oid:llä', function() {
      before(
        function() {
          return editor.property('sisältyyOpiskeluoikeuteen').property('oid').setValue(sisältävänOpiskeluoikeudenId)()
        },
        editor.saveChanges
      )

      it('tallennus onnistuu', function() {

      })

      describe('Sisältyvän opiskeluoikeuden näkyvyys sisältävän opiskeluoikeuden organisaatiolle', function() {
        before(
          Authentication().login('tallentaja'),
          page.openPage,
          page.oppijaHaku.searchAndSelect('280618-402H')
        )

        it('Lukuoikeudet on', function() {
          var year = new Date().getFullYear()
          expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.have.members([
            'Stadin ammattiopisto,Luonto- ja ympäristöalan perustutkinto(2012—2016, valmistunut)',
            'Omnian ammattiopisto,Autoalan perustutkinto(' + year + '—, läsnä)'
          ])
        })

        it('Kirjoitusoikeuksia ei ole', function() {
          expect(opinnot.opiskeluoikeusEditor('Stadin').isEditable()).to.equal(true)
          expect(opinnot.opiskeluoikeusEditor('Omnian').isEditable()).to.equal(false)
        })
      })
    })
  })
})
