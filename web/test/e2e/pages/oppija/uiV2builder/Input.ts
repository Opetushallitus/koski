import { expect } from '@playwright/test'
import { createControl } from './controls'

export const Input = createControl((self, child) => ({
  elem: child('input'),
  click: () => self.click(),
  value: () => child('input').inputValue(),
  set: async (value: string) => {
    const input = child('input')
    // fill() kirjoittaa arvon suoraan DOMiin ja lähettää input-tapahtuman.
    // Kentät ovat Reactin kontrolloimia: jos React ehtii renderöidä kentän
    // uudelleen juuri kirjoituksen ja tapahtuman välissä (esim. kun kentän
    // skeema saapuu palvelimelta), se palauttaa DOMiin oman arvonsa ja nollaa
    // sisäisen arvonseurantansa. Silloin onChange jää kokonaan tulematta,
    // kenttä jää tyhjäksi eikä lomakkeen tila päivity lainkaan — mistä seuraa
    // esim. pysyvästi disabloitu "Lisää"-painike.
    //
    // Siksi ei riitä kirjoittaa kerran: varmistetaan että arvo myös jäi
    // voimaan sen jälkeen kun kesken olleet renderöinnit ovat valmistuneet,
    // ja kirjoitetaan tarvittaessa uudelleen.
    await expect(async () => {
      await input.fill(value)
      await input.page().waitForTimeout(50)
      expect(await input.inputValue()).toBe(value)
    }).toPass({ timeout: 10000 })
  }
}))
