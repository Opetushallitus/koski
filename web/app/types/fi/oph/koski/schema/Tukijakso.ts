/**
 * Oppivelvollisen tukijaksot
 *
 * @see `fi.oph.koski.schema.Tukijakso`
 */
export type Tukijakso = {
  $class: 'fi.oph.koski.schema.Tukijakso'
  alku?: string
  loppu?: string
}

export const Tukijakso = (
  o: {
    alku?: string
    loppu?: string
  } = {}
): Tukijakso => ({ $class: 'fi.oph.koski.schema.Tukijakso', ...o })

Tukijakso.className = 'fi.oph.koski.schema.Tukijakso' as const

export const isTukijakso = (a: any): a is Tukijakso =>
  a?.$class === 'fi.oph.koski.schema.Tukijakso'
