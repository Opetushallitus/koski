import { Authentication } from '../page/authentication.js'
import { DocumentationPage } from '../page/documentationPage.js'
import { openPage, S, textsOf, toArray } from '../util/testHelpers.js'
import { expect } from '../util/chai.esm.js'

describe('Dokumentaatio', function () {
  let page = DocumentationPage()
  describe('Dokumentaatio-sivu', function () {
    before(Authentication().logout, page.openPage)
    it('näytetään', function () {
      expect(textsOf(S('h2'))).to.deep.equal(['Koski dokumentaatio'])
    })
  })

  describe('Skeeman kuvaus', function () {
    before(
      openPage(
        '/koski/dokumentaatio/koski-oppija-schema.html?entity=ammatillinenopiskeluoikeus'
      )
    )
    it('Toimii', function () {
      expect(toArray(S('h3')).length).to.be.above(10)
    })
  })
})
