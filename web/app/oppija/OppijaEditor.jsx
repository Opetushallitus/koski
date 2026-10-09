import React from 'baret'
import { addContext, modelData, modelItems } from '../editor/EditorModel'
import { OpiskeluoikeusEditor } from '../opiskeluoikeus/OpiskeluoikeusEditor'
import { useVirkailijaUiAdapterContext } from '../components-v2/interoperability/useUiAdapter'
import { Spinner } from '../components-v2/texts/Spinner'
import { currentLocation } from '../util/location.js'
import { flatMapArray } from '../util/util'
import OpiskeluoikeudetNavBar from './OpiskeluoikeudetNavBar'
import { TestIdRoot } from '../appstate/useTestId'
import { osoitteenOpiskeluoikeus } from '../appstate/sivunTila'

const sisältääOpiskeluoikeuden = (opiskeluoikeudenTyyppi, oid) =>
  modelData(opiskeluoikeudenTyyppi).opiskeluoikeudet.some((oppilaitos) =>
    oppilaitos.opiskeluoikeudet.some((oo) => oo.oid === oid)
  )

export const OppijaEditor = ({ model }) => {
  const oppijaOid = modelData(model, 'henkilö.oid')
  const selectedTyyppi = currentLocation().params.opiskeluoikeudenTyyppi
  const katseltavaOpiskeluoikeus = osoitteenOpiskeluoikeus()
  const opiskeluoikeusTyypit = modelItems(model, 'opiskeluoikeudet')

  // Versiolinkissä ei ole opiskeluoikeuden tyyppiä, joten avataan välilehti,
  // jolla katseltava opiskeluoikeus on.
  const selectedIndex = selectedTyyppi
    ? opiskeluoikeusTyypit.findIndex(
        (opiskeluoikeudenTyyppi) =>
          selectedTyyppi === modelData(opiskeluoikeudenTyyppi).tyyppi.koodiarvo
      )
    : katseltavaOpiskeluoikeus
      ? Math.max(
          0,
          opiskeluoikeusTyypit.findIndex((opiskeluoikeudenTyyppi) =>
            sisältääOpiskeluoikeuden(
              opiskeluoikeudenTyyppi,
              katseltavaOpiskeluoikeus
            )
          )
        )
      : 0

  const uiAdapter = useVirkailijaUiAdapterContext()

  return (
    <>
      <OpiskeluoikeudetNavBar
        {...{ oppijaOid, opiskeluoikeusTyypit, selectedIndex }}
      />
      {uiAdapter.isLoadingV2 && <Spinner className="loading" />}
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
                  const editor =
                    uiAdapter.getOpiskeluoikeusEditor(opiskeluoikeus)
                  return (
                    <li key={oppilaitosIndex + '-' + opiskeluoikeusIndex}>
                      {editor ? (
                        <TestIdRoot id={`oo.${opiskeluoikeusIndex}`}>
                          {editor}
                        </TestIdRoot>
                      ) : (
                        // Vanhan käyttöliittymän komponentit lukevat
                        // osoitteen vain renderöidessään, joten ne
                        // kiinnitetään uudelleen osoitteen muuttuessa.
                        <OpiskeluoikeusEditor
                          key={document.location.toString()}
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
