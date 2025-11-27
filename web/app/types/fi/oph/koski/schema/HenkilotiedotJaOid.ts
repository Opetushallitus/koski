/**
 * Henkilötiedot. Syötettäessä vaaditaan joko oppijanumero `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö
 *
 * @see `fi.oph.koski.schema.HenkilötiedotJaOid`
 */
export type HenkilötiedotJaOid = {
  $class: 'fi.oph.koski.schema.HenkilötiedotJaOid'
  kutsumanimi: string
  hetu?: string
  etunimet: string
  sukunimi: string
  oid: string
}

export const HenkilötiedotJaOid = (o: {
  kutsumanimi: string
  hetu?: string
  etunimet: string
  sukunimi: string
  oid: string
}): HenkilötiedotJaOid => ({
  $class: 'fi.oph.koski.schema.HenkilötiedotJaOid',
  ...o
})

HenkilötiedotJaOid.className = 'fi.oph.koski.schema.HenkilötiedotJaOid' as const

export const isHenkilötiedotJaOid = (a: any): a is HenkilötiedotJaOid =>
  a?.$class === 'fi.oph.koski.schema.HenkilötiedotJaOid'
