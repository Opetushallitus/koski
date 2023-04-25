import { Finnish } from '../../app/types/fi/oph/koski/schema/Finnish'
import { Oppija } from '../../app/types/fi/oph/koski/schema/Oppija'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../../app/types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { TäydellisetHenkilötiedot } from '../../app/types/fi/oph/koski/schema/TaydellisetHenkilotiedot'
import { test, expect } from './base'
import { kansalainen } from './setup/auth'

const hetut = {
  taiteenPerusopetusValmis: '110505A804B'
}

const oppilaitokset = {
  varsinaisSuomenKansalaisopisto: '1.2.246.562.10.31915273374'
}

test.describe('Suoritusjako', () => {
  test.beforeEach(
    async ({ fixtures, virkailijaLoginPage, kansalainenLoginPage }) => {
      await virkailijaLoginPage.apiLoginAsUser('kalle', 'kalle')
      await fixtures.reset()
      await virkailijaLoginPage.apiLogout()
      await kansalainenLoginPage.loginWithHetu(hetut.taiteenPerusopetusValmis)
    }
  )

  test.describe('Kansalaisen opintotiedot', () => {
    test.use({ storageState: kansalainen(hetut.taiteenPerusopetusValmis) })

    test('opiskeluoikeuden otsikko näkyy', async ({ kansalainenPage }) => {
      expect(await kansalainenPage.$.opiskeluoikeus.nimi.value()).toEqual(
        'Varsinais-Suomen kansanopisto, taiteen perusopetuksen laaja oppimäärä, musiikki (2021, hyväksytysti suoritettu)'
      )
    })

    test('voidaan jakaa suoritusjakona', async ({ kansalainenPage }) => {
      await kansalainenPage.openJaaSuoritustietoja()

      await kansalainenPage
        .suoritustietoLabel(
          oppilaitokset.varsinaisSuomenKansalaisopisto,
          'taiteenperusopetuksenlaajanoppimaaranperusopinnot',
          '999907'
        )
        .click()
      await kansalainenPage
        .suoritustietoLabel(
          oppilaitokset.varsinaisSuomenKansalaisopisto,
          'taiteenperusopetuksenlaajanoppimaaransyventavatopinnot',
          '999907'
        )
        .click()
      await kansalainenPage.jaaValitsemasiOpinnotButton().click()

      const suoritusotePage = await kansalainenPage.avaaSuoritusote()

      const jsonSuoritusotePage =
        await suoritusotePage.avaaJsonSuoritusotePage()

      const o: Oppija = JSON.parse(await jsonSuoritusotePage.jsonContent())
      const h = o.henkilö as TäydellisetHenkilötiedot

      expect(h.hetu).not.toBeDefined
      expect(h.kansalaisuus).not.toBeDefined

      expect(h.syntymäaika).toBe('2005-05-11')
      expect((h.äidinkieli?.nimi as Finnish).fi).toBe('suomi')

      expect(o.opiskeluoikeudet).toHaveLength(1)

      const ptst = o.opiskeluoikeudet[0]
        .suoritukset as Array<TaiteenPerusopetuksenPäätasonSuoritus>

      expect(ptst).toHaveLength(2)
    })
  })
})
