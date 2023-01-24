/**
 * Henkilötiedot. Syötettäessä vaaditaan joko oppijanumero `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö
 *
 * @see `fi.oph.koski.schema.HenkilötiedotJaOid`
 */
export type HenkilötiedotJaOid = {
  $class: 'fi.oph.koski.schema.HenkilötiedotJaOid'
  sukunimi: string
  oid: string
  kutsumanimi: string
  hetu?: string
  etunimet: string
}

export const HenkilötiedotJaOid = (o: {
  sukunimi: string
  oid: string
  kutsumanimi: string
  hetu?: string
  etunimet: string
}): HenkilötiedotJaOid => ({
  $class: 'fi.oph.koski.schema.HenkilötiedotJaOid',
  ...o
})

HenkilötiedotJaOid.className = 'fi.oph.koski.schema.HenkilötiedotJaOid' as const

export const isHenkilötiedotJaOid = (a: any): a is HenkilötiedotJaOid =>
  a?.$class === 'fi.oph.koski.schema.HenkilötiedotJaOid'
