/**
 * Palauttaa vapaan sivistystyön jatkuvan oppimisen ja työllisyyden palvelukeskuksen (JOTPA) raportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA`
 */
export type MassaluovutusQueryVSTJOTPA = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'vstJotpa'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryVSTJOTPA = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'vstJotpa'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryVSTJOTPA => ({
  type: 'vstJotpa',
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryVSTJOTPA.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA' as const

export const isMassaluovutusQueryVSTJOTPA = (
  a: any
): a is MassaluovutusQueryVSTJOTPA =>
  a?.$class === 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA'
