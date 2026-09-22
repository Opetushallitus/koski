/**
 * Palauttaa aikuisten perusopetuksen suoritustietojen tarkistusraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot`
 */
export type MassaluovutusQueryAikuistenPerusopetusSuoritustiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  osasuoritustenAikarajaus?: boolean
  type: 'aikuistenPerusopetusSuoritustiedot'
  alku: string
  raportinTyyppi: 'alkuvaihe' | 'päättövaihe' | 'oppiaineenoppimäärä'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryAikuistenPerusopetusSuoritustiedot = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  osasuoritustenAikarajaus?: boolean
  type?: 'aikuistenPerusopetusSuoritustiedot'
  alku: string
  raportinTyyppi: 'alkuvaihe' | 'päättövaihe' | 'oppiaineenoppimäärä'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryAikuistenPerusopetusSuoritustiedot => ({
  type: 'aikuistenPerusopetusSuoritustiedot',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryAikuistenPerusopetusSuoritustiedot.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot' as const

export const isMassaluovutusQueryAikuistenPerusopetusSuoritustiedot = (
  a: any
): a is MassaluovutusQueryAikuistenPerusopetusSuoritustiedot =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot'
