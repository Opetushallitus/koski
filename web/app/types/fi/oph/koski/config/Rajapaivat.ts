/**
 * Rajapäivät
 *
 * @see `fi.oph.koski.config.Rajapäivät`
 */
export type Rajapäivät = {
  $class: 'fi.oph.koski.config.Rajapäivät'
  ibLaajuusOpintopisteinäAlkaen: string
  diaLaajuusOpintopisteinäAlkaen: string
  kielitutkintotodistusAikaisinAlkamispäivä: string
}

export const Rajapäivät = (o: {
  ibLaajuusOpintopisteinäAlkaen: string
  diaLaajuusOpintopisteinäAlkaen: string
  kielitutkintotodistusAikaisinAlkamispäivä: string
}): Rajapäivät => ({ $class: 'fi.oph.koski.config.Rajapäivät', ...o })

Rajapäivät.className = 'fi.oph.koski.config.Rajapäivät' as const

export const isRajapäivät = (a: any): a is Rajapäivät =>
  a?.$class === 'fi.oph.koski.config.Rajapäivät'
