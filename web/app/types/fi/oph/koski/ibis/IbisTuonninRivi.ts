/**
 * IbisTuonninRivi
 *
 * @see `fi.oph.koski.ibis.IbisTuonninRivi`
 */
export type IbisTuonninRivi = {
  $class: 'fi.oph.koski.ibis.IbisTuonninRivi'
  tila: string
  diplomanOid?: string
  iboTunniste: string
  oppijaOid?: string
  virheet: Array<string>
  varoitukset: Array<string>
}

export const IbisTuonninRivi = (o: {
  tila: string
  diplomanOid?: string
  iboTunniste: string
  oppijaOid?: string
  virheet?: Array<string>
  varoitukset?: Array<string>
}): IbisTuonninRivi => ({
  $class: 'fi.oph.koski.ibis.IbisTuonninRivi',
  virheet: [],
  varoitukset: [],
  ...o
})

IbisTuonninRivi.className = 'fi.oph.koski.ibis.IbisTuonninRivi' as const

export const isIbisTuonninRivi = (a: any): a is IbisTuonninRivi =>
  a?.$class === 'fi.oph.koski.ibis.IbisTuonninRivi'
