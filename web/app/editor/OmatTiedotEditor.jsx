import React from 'baret'
import {addContext, modelData} from './EditorModel.js'
import {currentLocation} from '../location.js'
import {OpiskeluoikeusEditor} from './OpiskeluoikeusEditor.jsx'
import {modelItems, modelLookup} from './EditorModel'

export const OmatTiedotEditor = ({model}) => {
  let oppijaOid = modelData(model, 'henkilö.oid')
  let selectedOppilaitos = currentLocation().params.oppilaitos
  let opiskeluoikeudet = modelItems(model, 'opiskeluoikeudet')

  let selectedIndex = selectedOppilaitos
    ? opiskeluoikeudet.findIndex(oppilaitos => selectedOppilaitos === modelData(oppilaitos).oid)
    : 0

  let oppilaitos = modelLookup(model, 'opiskeluoikeudet.' + selectedIndex)
  return (
    <div>
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