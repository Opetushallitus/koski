/**
 * Osa-aikaisuusjakson kesto ja suuruus
 *
 * @see `fi.oph.koski.schema.OsaAikaisuusJakso`
 */
export type OsaAikaisuusJakso = {
  $class: 'fi.oph.koski.schema.OsaAikaisuusJakso'
  alku: string
  loppu?: string
  osaAikaisuus: number
}

export const OsaAikaisuusJakso = (o: {
  alku: string
  loppu?: string
  osaAikaisuus: number
}): OsaAikaisuusJakso => ({
  $class: 'fi.oph.koski.schema.OsaAikaisuusJakso',
  ...o
})

export const isOsaAikaisuusJakso = (a: any): a is OsaAikaisuusJakso =>
  a?.$class === 'fi.oph.koski.schema.OsaAikaisuusJakso'
