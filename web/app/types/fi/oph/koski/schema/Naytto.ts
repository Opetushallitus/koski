import { NäytönArviointi } from './NaytonArviointi'
import { NäytönSuorituspaikka } from './NaytonSuorituspaikka'
import { LocalizedString } from './LocalizedString'
import { NäytönSuoritusaika } from './NaytonSuoritusaika'

/**
 * Näyttö, jossa mukana työssäoppimisen tieto ja todistus-halukkuus.
 *
 * @see `fi.oph.koski.schema.Näyttö`
 */
export type Näyttö = {
  $class: 'fi.oph.koski.schema.Näyttö'
  arviointi?: NäytönArviointi
  suorituspaikka?: NäytönSuorituspaikka
  haluaaTodistuksen?: boolean
  työssäoppimisenYhteydessä: boolean
  kuvaus?: LocalizedString
  suoritusaika?: NäytönSuoritusaika
}

export const Näyttö = (
  o: {
    arviointi?: NäytönArviointi
    suorituspaikka?: NäytönSuorituspaikka
    haluaaTodistuksen?: boolean
    työssäoppimisenYhteydessä?: boolean
    kuvaus?: LocalizedString
    suoritusaika?: NäytönSuoritusaika
  } = {}
): Näyttö => ({
  työssäoppimisenYhteydessä: false,
  $class: 'fi.oph.koski.schema.Näyttö',
  ...o
})

Näyttö.className = 'fi.oph.koski.schema.Näyttö' as const

export const isNäyttö = (a: any): a is Näyttö =>
  a?.$class === 'fi.oph.koski.schema.Näyttö'
