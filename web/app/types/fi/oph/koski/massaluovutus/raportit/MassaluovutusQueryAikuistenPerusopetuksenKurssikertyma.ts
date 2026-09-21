/**
 * Palauttaa aikuisten perusopetuksen kurssikertymät-raportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma`
 */
export type MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'aikuistenPerusopetuksenKurssikertyma'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'aikuistenPerusopetuksenKurssikertyma'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma => ({
  type: 'aikuistenPerusopetuksenKurssikertyma',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma' as const

export const isMassaluovutusQueryAikuistenPerusopetuksenKurssikertyma = (
  a: any
): a is MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma'
