import React from 'baret'
import { addContext, modelData, modelItems } from '../editor/EditorModel'
import { currentLocation } from '../util/location.js'
import { OpiskeluoikeusEditor } from '../opiskeluoikeus/OpiskeluoikeusEditor'
import OpiskeluoikeudetNavBar from './OpiskeluoikeudetNavBar'
import { flatMapArray } from '../util/util'

export const OppijaEditor = ({ model }) => {
  const oppijaOid = modelData(model, 'henkilö.oid')
  const selectedTyyppi = currentLocation().params.opiskeluoikeudenTyyppi
  const opiskeluoikeusTyypit = modelItems(model, 'opiskeluoikeudet')

  const selectedIndex = selectedTyyppi
    ? opiskeluoikeusTyypit.findIndex(
        (opiskeluoikeudenTyyppi) =>
          selectedTyyppi === modelData(opiskeluoikeudenTyyppi).tyyppi.koodiarvo
      )
    : 0

  return (
    <div>
      <OpiskeluoikeudetNavBar
        {...{ oppijaOid, opiskeluoikeusTyypit, selectedIndex }}
      />
      <ul className="opiskeluoikeuksientiedot">
        {flatMapArray(
          modelItems(
            model,
            'opiskeluoikeudet.' + selectedIndex + '.opiskeluoikeudet'
          ),
          (oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) => {
            return modelItems(
              oppilaitoksenOpiskeluoikeudet,
              'opiskeluoikeudet'
            ).map((opiskeluoikeus, opiskeluoikeusIndex) => (
              <li key={oppilaitosIndex + '-' + opiskeluoikeusIndex}>
                <OpiskeluoikeusEditor
                  model={addContext(opiskeluoikeus, {
                    oppijaOid,
                    opiskeluoikeusIndex
                  })}
                />
              </li>
            ))
          }
        )}
      </ul>
    </div>
  )
}
