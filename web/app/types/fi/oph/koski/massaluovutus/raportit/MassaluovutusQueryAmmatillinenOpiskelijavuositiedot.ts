/**
 * Palauttaa ammatillisen koulutuksen opiskelijavuositiedot-raportin.
 * Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot`
 */
export type MassaluovutusQueryAmmatillinenOpiskelijavuositiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'ammatillinenOpiskelijavuositiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryAmmatillinenOpiskelijavuositiedot = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'ammatillinenOpiskelijavuositiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryAmmatillinenOpiskelijavuositiedot => ({
  type: 'ammatillinenOpiskelijavuositiedot',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryAmmatillinenOpiskelijavuositiedot.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot' as const

export const isMassaluovutusQueryAmmatillinenOpiskelijavuositiedot = (
  a: any
): a is MassaluovutusQueryAmmatillinenOpiskelijavuositiedot =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot'
