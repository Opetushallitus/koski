/**
 * Henkilötiedot. Syötettäessä vaaditaan joko oppijanumero `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö
 * Henkilö, jonka oppijanumero 'oid' ei ole tiedossa. Tietoja syötettäessä luodaan mahdollisesti uusi henkilö Henkilöpalveluun, jolloin henkilölle muodostuu oppijanumero
 *
 * @see `fi.oph.koski.schema.UusiHenkilö`
 */
export type UusiHenkilö = {
  $class: 'fi.oph.koski.schema.UusiHenkilö'
  hetu: string
  etunimet: string
  kutsumanimi?: string
  sukunimi: string
}

export const UusiHenkilö = (o: {
  hetu: string
  etunimet: string
  kutsumanimi?: string
  sukunimi: string
}): UusiHenkilö => ({ $class: 'fi.oph.koski.schema.UusiHenkilö', ...o })

export const isUusiHenkilö = (a: any): a is UusiHenkilö =>
  a?.$class === 'UusiHenkilö'
