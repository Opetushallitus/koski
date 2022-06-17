import React from 'baret'
import {addContext, modelData} from '../editor/EditorModel'
import {currentLocation} from '../util/location.js'
import {OpiskeluoikeusEditor} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import {modelItems} from '../editor/EditorModel'
import OpiskeluoikeudetNavBar from './OpiskeluoikeudetNavBar'
import {flatMapArray} from '../util/util'

export const OppijaEditor = ({model}) => {
  let oppijaOid = modelData(model, 'henkilö.oid')
  let selectedTyyppi = currentLocation().params.opiskeluoikeudenTyyppi
  var opiskeluoikeusTyypit = modelItems(model, 'opiskeluoikeudet')

  let selectedIndex = selectedTyyppi
    ? opiskeluoikeusTyypit.findIndex((opiskeluoikeudenTyyppi) => selectedTyyppi == modelData(opiskeluoikeudenTyyppi).tyyppi.koodiarvo)
    : 0

  return (
    <div>
      <OpiskeluoikeudetNavBar {...{ oppijaOid, opiskeluoikeusTyypit, selectedIndex }}/>
      <ul className="opiskeluoikeuksientiedot">
        {
          flatMapArray(modelItems(model, 'opiskeluoikeudet.' + selectedIndex + '.opiskeluoikeudet'), (oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) => {
            return modelItems(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) =>
              (<li key={ oppilaitosIndex + '-' + opiskeluoikeusIndex }>
                <OpiskeluoikeusEditor
                  model={ addContext(opiskeluoikeus, { oppijaOid: oppijaOid, opiskeluoikeusIndex }) }
                />
              </li>)
            )
          })
        }
      </ul>
    </div>)
}
