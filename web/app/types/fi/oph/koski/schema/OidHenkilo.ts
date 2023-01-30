/**
 * Henkilötiedot. Syötettäessä vaaditaan joko oppijanumero `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö
 * Henkilö, jonka oppijanumero 'oid' on tiedossa. Tietoja syötettäessä henkilö haetaan henkilöpalvelusta
 *
 * @see `fi.oph.koski.schema.OidHenkilö`
 */
export type OidHenkilö = {
  $class: 'fi.oph.koski.schema.OidHenkilö'
  oid: string
}

export const OidHenkilö = (o: { oid: string }): OidHenkilö => ({
  $class: 'fi.oph.koski.schema.OidHenkilö',
  ...o
})

OidHenkilö.className = 'fi.oph.koski.schema.OidHenkilö' as const

export const isOidHenkilö = (a: any): a is OidHenkilö =>
  a?.$class === 'fi.oph.koski.schema.OidHenkilö'
