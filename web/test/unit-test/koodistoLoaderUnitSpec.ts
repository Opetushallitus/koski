import './setup/browserGlobals'
import * as assert from 'assert'
import * as E from 'fp-ts/Either'
import { KoodistoLoader } from '../../app/appstate/koodisto'
import { GroupedKoodistot } from '../../app/types/fi/oph/koski/typemodel/GroupedKoodistot'
import { Koodistokoodiviite } from '../../app/types/fi/oph/koski/schema/Koodistokoodiviite'

/**
 * KoodistoLoader pitää kirjaa käynnissä olevista hauista pending-taulussa, jotta
 * rinnakkainen kutsuja odottaa toisen aloittamaa hakua. Jos haku heittää
 * poikkeuksen, taulu on siivottava — muuten sinne jää hylätty lupaus, jota
 * jokainen myöhempi kutsu jää odottamaan ja hylkäytyy heti. Käytännössä
 * pudotusvalikko jäisi pysyvästi disabloiduksi, koska se disabloi syötteensä
 * kun vaihtoehtoja ei ole.
 */
describe('KoodistoLoader', () => {
  it('ei jää jumiin, jos haku heittää poikkeuksen', async () => {
    let kutsuja = 0
    const loader = new KoodistoLoader(async () => {
      kutsuja++
      throw new Error('verkkovirhe')
    })

    // Ensimmäinen kutsu ei saa hylkäytyä, vaan käsittelee virheen.
    await loader.loadKoodistot(['kielivalikoima'])
    assert.strictEqual(kutsuja, 1)

    // Toinen kutsu ei saa jäädä odottamaan edellisen hylättyä lupausta, vaan
    // yrittää hakua uudelleen.
    await loader.loadKoodistot(['kielivalikoima'])
    assert.strictEqual(kutsuja, 2)
  })

  it('täyttää koodistot onnistuneen haun jälkeen', async () => {
    const loader = new KoodistoLoader(async () =>
      E.right({
        status: 200,
        data: GroupedKoodistot({
          koodistot: {
            kielivalikoima: [
              Koodistokoodiviite({
                koodistoUri: 'kielivalikoima',
                koodiarvo: 'SV'
              })
            ]
          }
        })
      })
    )

    await loader.loadKoodistot(['kielivalikoima'])

    assert.strictEqual(
      loader.findKoodi('kielivalikoima', 'SV')?.koodiarvo,
      'SV'
    )
  })
})
