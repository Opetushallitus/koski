import {
  MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus,
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
} from './MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
import {
  YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus,
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
} from './YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'

/**
 * OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus
 *
 * @see `fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus`
 */
export type OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus =
  | MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus
  | YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus

export const isOsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus = (
  a: any
): a is OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus =>
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus(
    a
  ) ||
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus(
    a
  )
