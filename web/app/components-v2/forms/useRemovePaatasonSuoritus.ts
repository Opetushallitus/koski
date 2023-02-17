import * as O from 'fp-ts/Option'
import * as A from 'fp-ts/Array'
import * as E from 'fp-ts/Either'
import * as Eq from 'fp-ts/Eq'
import * as TE from 'fp-ts/TaskEither'
import { identity, pipe } from 'fp-ts/lib/function'
import { useCallback } from 'react'
import { useApiMethod } from '../../api-fetch'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { deletePäätasonSuoritus } from '../../util/koskiApi'
import {
  getOpiskeluoikeusOid,
  getVersionumero,
  mergeOpiskeluoikeusVersionumero,
  PäätasonSuoritusOf
} from '../../util/opiskeluoikeus'
import { FormModel } from './FormModel'
import { taskifyApiCall } from '../../util/fp/either'

const remove = taskifyApiCall(deletePäätasonSuoritus)

export const useRemovePäätasonSuoritus = <T extends Opiskeluoikeus>(
  form: FormModel<T>,
  päätasonSuoritus: PäätasonSuoritusOf<T>,
  päätasonSuoritusEq: Eq.Eq<PäätasonSuoritusOf<T>>,
  onRemove: () => void
) => {
  const removePäätasonSuoritus = useCallback(async () => {
    onRemove()

    pipe(
      A.findFirst((a: PäätasonSuoritusOf<T>) =>
        päätasonSuoritusEq.equals(a, päätasonSuoritus)
      )(form.initialState.suoritukset as PäätasonSuoritusOf<T>[]),
      O.chain((suoritusAtBackend) => {
        const oo = form.state
        const oid = getOpiskeluoikeusOid(oo)
        const versio = getVersionumero(oo)
        return oid && versio
          ? O.some(remove(oid, versio, suoritusAtBackend))
          : O.none
      }),
      O.map(
        TE.map((ooVersiot) => {
          form.updateAt(
            form.root,
            mergeOpiskeluoikeusVersionumero(ooVersiot.data)
          )
        })
      )
    )
  }, [form, onRemove, päätasonSuoritus, päätasonSuoritusEq])

  return removePäätasonSuoritus
}
