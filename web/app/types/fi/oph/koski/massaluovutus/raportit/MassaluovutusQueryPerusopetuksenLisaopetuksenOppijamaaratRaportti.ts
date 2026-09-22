/**
 * Palauttaa perusopetuksen lisäopetuksen oppijamääräraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti`
 */
export type MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti =
  {
    $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti'
    paiva: string
    language?: 'fi' | 'sv' | 'en'
    type: 'perusopetuksenLisaopetuksenOppijamaaratRaportti'
    password?: string
    organisaatioOid?: string
    format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  }

export const MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti =
  (o: {
    paiva: string
    language?: 'fi' | 'sv' | 'en'
    type?: 'perusopetuksenLisaopetuksenOppijamaaratRaportti'
    password?: string
    organisaatioOid?: string
    format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  }): MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti => ({
    type: 'perusopetuksenLisaopetuksenOppijamaaratRaportti',
    $class:
      'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti',
    format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    ...o
  })

MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti' as const

export const isMassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti =
  (
    a: any
  ): a is MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti =>
    a?.$class ===
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti'
