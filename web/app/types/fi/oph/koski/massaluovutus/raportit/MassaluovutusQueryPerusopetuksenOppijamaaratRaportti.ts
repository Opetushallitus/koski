/**
 * Palauttaa perusopetuksen oppijamäärät-raportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti`
 */
export type MassaluovutusQueryPerusopetuksenOppijamaaratRaportti = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti'
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type: 'perusopetuksenOppijamaaratRaportti'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryPerusopetuksenOppijamaaratRaportti = (o: {
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'perusopetuksenOppijamaaratRaportti'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryPerusopetuksenOppijamaaratRaportti => ({
  type: 'perusopetuksenOppijamaaratRaportti',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryPerusopetuksenOppijamaaratRaportti.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti' as const

export const isMassaluovutusQueryPerusopetuksenOppijamaaratRaportti = (
  a: any
): a is MassaluovutusQueryPerusopetuksenOppijamaaratRaportti =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti'
