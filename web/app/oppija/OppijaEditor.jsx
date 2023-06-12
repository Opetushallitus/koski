import React from 'baret'
import { addContext, modelData, modelItems } from '../editor/EditorModel'
import { OpiskeluoikeusEditor } from '../opiskeluoikeus/OpiskeluoikeusEditor'
import { useVirkailijaUiAdapter } from '../components-v2/interoperability/useUiAdapter'
import { currentLocation } from '../util/location.js'
import { flatMapArray } from '../util/util'
import OpiskeluoikeudetNavBar from './OpiskeluoikeudetNavBar'

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

  const uiAdapter = useVirkailijaUiAdapter(model)

  return (
    <>
      <OpiskeluoikeudetNavBar
        {...{ oppijaOid, opiskeluoikeusTyypit, selectedIndex }}
      />
      {!uiAdapter.isLoadingV2 && (
        <div>
          <ul
            className="opiskeluoikeuksientiedot"
            data-testid="opiskeluoikeuksientiedot"
          >
            {flatMapArray(
              modelItems(
                model,
                'opiskeluoikeudet.' + selectedIndex + '.opiskeluoikeudet'
              ),
              (oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) => {
                return modelItems(
                  oppilaitoksenOpiskeluoikeudet,
                  'opiskeluoikeudet'
                ).map((opiskeluoikeus, opiskeluoikeusIndex) => {
                  const Editor =
                    uiAdapter.getOpiskeluoikeusEditor(opiskeluoikeus)
                  return (
                    <li key={oppilaitosIndex + '-' + opiskeluoikeusIndex}>
                      {Editor ? (
                        <Editor />
                      ) : (
                        <OpiskeluoikeusEditor
                          model={addContext(opiskeluoikeus, {
                            oppijaOid,
                            opiskeluoikeusIndex
                          })}
                        />
                      )}
                    </li>
                  )
                })
              }
            )}
          </ul>
        </div>
      )}
    </>
  )
}
