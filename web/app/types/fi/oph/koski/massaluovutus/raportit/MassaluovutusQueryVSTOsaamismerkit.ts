/**
 * Palauttaa vapaan sivistystyön osaamismerkkien opiskeluoikeus- ja suoritustietojen tarkistusraportin. Raportille valitaan opiskeluoikeudet, jotka ovat olleet voimassa annetulla aikajaksolla.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTOsaamismerkit`
 */
export type MassaluovutusQueryVSTOsaamismerkit = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTOsaamismerkit'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'vstOsaamismerkit'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryVSTOsaamismerkit = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'vstOsaamismerkit'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryVSTOsaamismerkit => ({
  type: 'vstOsaamismerkit',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTOsaamismerkit',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryVSTOsaamismerkit.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTOsaamismerkit' as const

export const isMassaluovutusQueryVSTOsaamismerkit = (
  a: any
): a is MassaluovutusQueryVSTOsaamismerkit =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTOsaamismerkit'
