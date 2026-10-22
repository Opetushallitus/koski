/**
 * Palauttaa oppivelvollisille suunnatun vapaan sivistystyön koulutuksen (KOPS) opiskeluoikeus- ja suoritustietojen tarkistusraportin. Raportille valitaan opiskeluoikeudet, jotka ovat olleet voimassa annetulla aikajaksolla.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTKOPS`
 */
export type MassaluovutusQueryVSTKOPS = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTKOPS'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'vstKops'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryVSTKOPS = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'vstKops'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryVSTKOPS => ({
  type: 'vstKops',
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTKOPS',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryVSTKOPS.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTKOPS' as const

export const isMassaluovutusQueryVSTKOPS = (
  a: any
): a is MassaluovutusQueryVSTKOPS =>
  a?.$class === 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTKOPS'
