import * as Eq from 'fp-ts/Eq'
import { useCallback } from 'react'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { deleteAt } from '../../util/fp/arrays'
import { deletePäätasonSuoritus } from '../../util/koskiApi'
import {
  getOpiskeluoikeusOid,
  getVersionumero,
  mergeOpiskeluoikeusVersionumeroAndRefetch,
  PäätasonSuoritusOf
} from '../../util/opiskeluoikeus'
import { FormModel } from './FormModel'

export const useRemovePäätasonSuoritus = <T extends Opiskeluoikeus>(
  form: FormModel<T>,
  päätasonSuoritus: PäätasonSuoritusOf<T>,
  päätasonSuoritusEq: Eq.Eq<PäätasonSuoritusOf<T>>,
  onRemove: () => void
) => {
  const removePäätasonSuoritus = useCallback(async () => {
    onRemove()

    // Backend poistaa vain täsmälleen tallennetun kaltaisen suorituksen, joten
    // poistettava haetaan ladatusta tilasta. Jos suoritusta ei löydy, se on
    // lisätty tässä muokkauksessa eikä sitä tarvitse poistaa backendiltä.
    const tallennetut = form.originalState
      .suoritukset as PäätasonSuoritusOf<T>[]
    const index = tallennetut.findIndex((s) =>
      päätasonSuoritusEq.equals(s, päätasonSuoritus)
    )
    const oid = getOpiskeluoikeusOid(form.state)
    const versio = getVersionumero(form.state)
    if (index < 0 || !oid || versio === undefined) {
      return
    }

    // Poisto tallentuu heti, joten lomake viedään tallennuksen tavoin backendin
    // tilaan. Muuten muokkauksen peruminen palauttaisi poistetun suorituksen.
    const opiskeluoikeusPoistonJälkeen = {
      ...form.originalState,
      suoritukset: deleteAt(tallennetut, index)
    }
    form.save(
      () => deletePäätasonSuoritus(oid, versio, tallennetut[index]),
      (ooVersiot) => async () => {
        const tallennettu = await mergeOpiskeluoikeusVersionumeroAndRefetch<T>(
          ooVersiot
        )(opiskeluoikeusPoistonJälkeen)
        // Backend palauttaa suoritukset tallennusjärjestyksessä, joka voi
        // poiketa editorin järjestämistä välilehdistä.
        return {
          ...tallennettu,
          suoritukset: samaanJärjestykseen(
            tallennettu.suoritukset as PäätasonSuoritusOf<T>[],
            opiskeluoikeusPoistonJälkeen.suoritukset,
            päätasonSuoritusEq
          )
        }
      }
    )
  }, [form, onRemove, päätasonSuoritus, päätasonSuoritusEq])

  return removePäätasonSuoritus
}

const samaanJärjestykseen = <S>(
  suoritukset: S[],
  järjestys: S[],
  eq: Eq.Eq<S>
): S[] => {
  const sijainti = (s: S) => {
    const i = järjestys.findIndex((j) => eq.equals(j, s))
    return i < 0 ? järjestys.length : i
  }
  return [...suoritukset].sort((a, b) => sijainti(a) - sijainti(b))
}
