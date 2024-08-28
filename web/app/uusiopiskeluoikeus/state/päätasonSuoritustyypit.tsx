import { useMemo } from 'react'
import * as A from 'fp-ts/Array'
import * as Eq from 'fp-ts/Eq'
import * as string from 'fp-ts/string'
import { useKoodisto } from '../../appstate/koodisto'
import {
  SelectOption,
  koodiviiteToOption
} from '../../components-v2/controls/Select'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { UusiOpiskeluoikeusDialogState } from './state'

export const usePäätasonSuoritustyypit = (
  state: UusiOpiskeluoikeusDialogState
): Array<SelectOption<Koodistokoodiviite<'suorituksentyyppi'>>> => {
  const koodisto = useKoodisto('suorituksentyyppi')?.map((k) => k.koodiviite)
  const ooTyyppi = state.opiskeluoikeus.value
  const ooMapping = state.ooMapping

  return useMemo(() => {
    const ooClassInfo =
      ooTyyppi !== undefined && ooMapping
        ? ooMapping.find((c) => c.tyyppi === ooTyyppi.koodiarvo)
        : undefined
    const koodit = ooClassInfo
      ? ooClassInfo.suoritukset.flatMap((s) => {
          const viite = koodisto?.find((k) => k.koodiarvo === s.tyyppi)
          return viite ? [viite] : []
        })
      : []
    return distinctKeys(koodit.map(koodiviiteToOption))
  }, [koodisto, ooMapping, ooTyyppi])
}

const SelectOptionKeyEq = <O extends SelectOption<any>>() =>
  Eq.contramap((o: O) => o.key)(string.Eq)

const distinctKeys = <O extends SelectOption<any>>(os: O[]) =>
  A.uniq(SelectOptionKeyEq<O>())(os)
