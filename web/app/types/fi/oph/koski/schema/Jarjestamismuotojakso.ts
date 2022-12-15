import { Järjestämismuoto } from './Jarjestamismuoto'

/**
 * Järjestämismuotojakso
 *
 * @see `fi.oph.koski.schema.Järjestämismuotojakso`
 */
export type Järjestämismuotojakso = {
  $class: 'fi.oph.koski.schema.Järjestämismuotojakso'
  alku: string
  loppu?: string
  järjestämismuoto: Järjestämismuoto
}

export const Järjestämismuotojakso = (o: {
  alku: string
  loppu?: string
  järjestämismuoto: Järjestämismuoto
}): Järjestämismuotojakso => ({
  $class: 'fi.oph.koski.schema.Järjestämismuotojakso',
  ...o
})

export const isJärjestämismuotojakso = (a: any): a is Järjestämismuotojakso =>
  a?.$class === 'Järjestämismuotojakso'
