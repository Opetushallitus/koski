/**
 * Palauttaa muun ammatillisen koulutuksen raportin.
 * Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen`
 */
export type MassaluovutusQueryMuuAmmatillinen = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'muuAmmatillinen'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryMuuAmmatillinen = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'muuAmmatillinen'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryMuuAmmatillinen => ({
  type: 'muuAmmatillinen',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryMuuAmmatillinen.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen' as const

export const isMassaluovutusQueryMuuAmmatillinen = (
  a: any
): a is MassaluovutusQueryMuuAmmatillinen =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen'
