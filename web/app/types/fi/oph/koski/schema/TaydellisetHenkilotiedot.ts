import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Henkilötiedot. Syötettäessä vaaditaan joko oppijanumero `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö
 * Täydet henkilötiedot. Tietoja haettaessa Koskesta saadaan aina täydet henkilötiedot
 *
 * @see `fi.oph.koski.schema.TäydellisetHenkilötiedot`
 */
export type TäydellisetHenkilötiedot = {
  $class: 'fi.oph.koski.schema.TäydellisetHenkilötiedot'
  äidinkieli?: Koodistokoodiviite<'kieli', string>
  sukunimi: string
  oid: string
  syntymäaika?: string
  kutsumanimi: string
  kansalaisuus?: Array<Koodistokoodiviite<'maatjavaltiot2', string>>
  turvakielto?: boolean
  hetu?: string
  etunimet: string
}

export const TäydellisetHenkilötiedot = (o: {
  äidinkieli?: Koodistokoodiviite<'kieli', string>
  sukunimi: string
  oid: string
  syntymäaika?: string
  kutsumanimi: string
  kansalaisuus?: Array<Koodistokoodiviite<'maatjavaltiot2', string>>
  turvakielto?: boolean
  hetu?: string
  etunimet: string
}): TäydellisetHenkilötiedot => ({
  $class: 'fi.oph.koski.schema.TäydellisetHenkilötiedot',
  ...o
})

export const isTäydellisetHenkilötiedot = (
  a: any
): a is TäydellisetHenkilötiedot =>
  a?.$class === 'fi.oph.koski.schema.TäydellisetHenkilötiedot'
