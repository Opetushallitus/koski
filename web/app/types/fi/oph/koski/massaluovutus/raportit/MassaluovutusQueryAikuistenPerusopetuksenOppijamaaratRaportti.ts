/**
 * Palauttaa aikuisten perusopetuksen oppijamäärät-raportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti`
 */
export type MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti'
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type: 'aikuistenPerusopetuksenOppijamaaratRaportti'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti =
  (o: {
    paiva: string
    language?: 'fi' | 'sv' | 'en'
    type?: 'aikuistenPerusopetuksenOppijamaaratRaportti'
    password?: string
    organisaatioOid?: string
    format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  }): MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti => ({
    type: 'aikuistenPerusopetuksenOppijamaaratRaportti',
    $class:
      'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti',
    format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    ...o
  })

MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti' as const

export const isMassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti = (
  a: any
): a is MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti'
