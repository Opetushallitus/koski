import React from 'baret'
import {addContext, modelData} from '../editor/EditorModel.js'
import {currentLocation} from '../util/location.js'
import {OpiskeluoikeusEditor} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import {modelItems, modelLookup} from '../editor/EditorModel'
import OpiskeluoikeudetOppilaitoksittainNavBar from './OpiskeluoikeudetOppilaitoksittainNavBar'

export const OmatTiedotEditor = ({model}) => {
  let oppijaOid = modelData(model, 'henkilö.oid')
  let selectedOppilaitos = currentLocation().params.oppilaitos
  let oppilaitokset = modelItems(model, 'opiskeluoikeudet')

  let selectedIndex = selectedOppilaitos
    ? oppilaitokset.findIndex(oppilaitos => selectedOppilaitos === modelData(oppilaitos, 'oppilaitos').oid)
    : 0

  let oppilaitos = modelLookup(model, 'opiskeluoikeudet.' + selectedIndex)
  return (
    <div>
      <OpiskeluoikeudetOppilaitoksittainNavBar {...{ opiskeluoikeudet: oppilaitokset, selectedIndex }}/>
      <ul className="opiskeluoikeuksientiedot">
        {
          modelItems(oppilaitos, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) => {
            return (<li key={ selectedIndex + '-' + opiskeluoikeusIndex }>
                <OpiskeluoikeusEditor
                  model={ addContext(opiskeluoikeus, { oppijaOid: oppijaOid, opiskeluoikeusIndex }) }
                />
              </li>)
          })
        }
      </ul>
    </div>)
}