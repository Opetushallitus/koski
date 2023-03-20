import { PulssiPage } from '../page/pulssiPage.js'
import { expect } from '../util/chai.esm.js'

describe('Pulssi', function () {
  let page = PulssiPage()
  describe('Koski-pulssi', function () {
    before(page.openPage)
    it('näytetään', function () {
      expect(page.isVisible()).to.equal(true)
    })
  })
  describe('Metriikat', function () {
    before(page.openPage)
    it('Opiskeluoikeuksien kokonaislukumäärä näytetään', function () {
      expect(page.metric('opiskeluoikeudet-total').value() >= 0).to.equal(true)
    })
    it('Opiskeluoikeudet koulutusmuodoittain näytetään', function () {
      expect(
        page.metric('opiskeluoikeudet-koulutusmuodoittain').sum()
      ).to.equal(page.metric('opiskeluoikeudet-total').value())
    })
    it('Suoritettujen koulutusten määrä näytetään', function () {
      expect(page.metric('valmiit-tutkinnot-total').value() > 0).to.equal(true)
    })
    it('Suoritettujen koulutusten määrä tyypeittäin näytetään', function () {
      expect(
        page.metric('valmiit-tutkinnot-koulutusmuodoittain').sum()
      ).to.equal(page.metric('valmiit-tutkinnot-total').value())
    })
    it('Saavutettavuus näytetään', function () {
      expect(page.metric('saavutettavuus', 'section').value() >= 0).to.equal(
        true
      )
    })
    it('Operaatiot näytetään', function () {
      expect(page.metric('operaatiot', 'section').value() >= 0).to.equal(true)
    })
    it('Operaatiot tyypeittäin näytetään', function () {
      expect(page.metric('operaatiot', 'section').sum()).to.equal(
        page.metric('operaatiot', 'section').value()
      )
    })
  })
})
