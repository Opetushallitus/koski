import React from 'baret'
import Bacon from 'baconjs'
import Http from '../util/http'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import { doActionWhileMounted, toObservable } from '../util/util'

export default ({ diaarinumero, suoritustapaAtom, title }) => {
  return (
    <div>
      {toObservable(diaarinumero).flatMapLatest((d) => {
        const suoritustavatP = (
          d
            ? Http.cachedGet(
                `/koski/api/tutkinnonperusteet/suoritustavat/${encodeURIComponent(
                  d
                )}`
              )
            : Bacon.constant([])
        ).toProperty()
        return (
          <span>
            <KoodistoDropdown
              className="suoritustapa"
              title={title}
              options={suoritustavatP}
              selected={suoritustapaAtom}
            />{' '}
            {doActionWhileMounted(
              Bacon.combineAsArray(suoritustapaAtom, suoritustavatP),
              ([suoritustapa, suoritustavat]) => {
                const currentOneFound =
                  suoritustapa &&
                  suoritustavat
                    .map((k) => k.koodiarvo)
                    .includes(suoritustapa.koodiarvo)
                if (!currentOneFound) {
                  if (suoritustavat.length === 1) {
                    suoritustapaAtom.set(suoritustavat[0])
                  } else if (suoritustapa) {
                    suoritustapaAtom.set(undefined)
                  }
                }
              }
            )}
          </span>
        )
      })}
    </div>
  )
}
