import * as $ from 'optics-ts'
import { useMemo } from 'react'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'

/**
 * Isomorfismi jolla voi viitata LocalizedStringin kaikkiin kielikenttiin
 */
export const allLanguages = $.optic_<LocalizedString>().iso(
  (localized: LocalizedString) =>
    (localized as any).fi || (localized as any).sv || localized.en,
  (str: string) =>
    Finnish({
      fi: str,
      sv: str,
      en: str
    })
)

/**
 * Opiskeluoikeuden päätason suoritus
 */
export const päätasonSuoritus = <T extends Opiskeluoikeus = Opiskeluoikeus>(
  index: number = 0
) => $.optic_<T>().prop('suoritukset').at(index)

export const usePäätasonSuoritus = <T extends Opiskeluoikeus = Opiskeluoikeus>(
  index: number = 0
) => useMemo(() => päätasonSuoritus<T>(index), [index])
