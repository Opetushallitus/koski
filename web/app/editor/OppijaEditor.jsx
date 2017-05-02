import React from 'baret'
import Atom from 'bacon.atom'
import {modelData, modelTitle, addContext} from './EditorModel.js'
import Link from '../Link.jsx'
import {currentLocation} from '../location.js'
import {yearFromIsoDateString} from '../date'
import {OpiskeluoikeusEditor} from './OpiskeluoikeusEditor.jsx'
import {SuoritusEditor} from './SuoritusEditor.jsx'
import {modelItems} from './EditorModel'
import {UusiOpiskeluoikeusPopup} from './UusiOpiskeluoikeusPopup.jsx'
import {putOppija} from '../CreateOppija.jsx'
import {reloadOppija} from '../Oppija.jsx'
import {userP} from '../user'
import {navigateTo} from '../location'

export const OppijaEditor = ({model}) => {
    let addingAtom = Atom(false)
    let toggleAdd = () => addingAtom.modify(x => !x)
    let oppijaOid = modelData(model, 'henkilö.oid')
    let selectedTyyppi = currentLocation().params.opiskeluoikeudenTyyppi
    var opiskeluoikeusTyypit = modelItems(model, 'opiskeluoikeudet')

    let selectedIndex = selectedTyyppi
      ? opiskeluoikeusTyypit.findIndex((opiskeluoikeudenTyyppi) => selectedTyyppi == modelData(opiskeluoikeudenTyyppi).tyyppi.koodiarvo)
      : 0

    let canAddOpiskeluoikeusP = userP.map(u => !!u.hasWriteAccess)
    let addOpiskeluoikeus = (opiskeluoikeus) => {
      if (!opiskeluoikeus) {
        addingAtom.set(false)
      } else {
        var oppija = {
          henkilö: { oid: oppijaOid},
          opiskeluoikeudet: [opiskeluoikeus]
        }
        var tyyppi = opiskeluoikeus.tyyppi.koodiarvo
        console.log(tyyppi)
        putOppija(oppija).onValue(() => {
          reloadOppija()
          navigateTo('?opiskeluoikeudenTyyppi=' + tyyppi)
        })
      }
    }

    return (
      <div>
        <ul className="opiskeluoikeustyypit">
          {
            opiskeluoikeusTyypit.map((opiskeluoikeudenTyyppi, tyyppiIndex) => {
              let selected = tyyppiIndex == selectedIndex
              let koodiarvo = modelData(opiskeluoikeudenTyyppi).tyyppi.koodiarvo
              let className = selected ? koodiarvo + ' selected' : koodiarvo
              let content = (<div>
                <div className="opiskeluoikeustyyppi">{ modelTitle(opiskeluoikeudenTyyppi, 'tyyppi') }</div>
                <ul className="oppilaitokset">
                  {
                    modelItems(opiskeluoikeudenTyyppi, 'opiskeluoikeudet').map((oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) =>
                      <li key={oppilaitosIndex}>
                        <span className="oppilaitos">{modelTitle(oppilaitoksenOpiskeluoikeudet, 'oppilaitos')}</span>
                        <ul className="opiskeluoikeudet">
                          {
                            modelItems(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) =>
                              modelItems(opiskeluoikeus, 'suoritukset').filter(SuoritusEditor.näytettäväPäätasonSuoritus).map((suoritus, suoritusIndex) =>
                                <li className="opiskeluoikeus" key={opiskeluoikeusIndex + '-' + suoritusIndex}>
                                  <span className="koulutus inline-text">{ modelTitle(suoritus, 'tyyppi') }</span>
                                  { modelData(opiskeluoikeus, 'alkamispäivä')
                                    ? <span className="inline-text">
                                        <span className="alku pvm">{yearFromIsoDateString(modelTitle(opiskeluoikeus, 'alkamispäivä'))}</span>-
                                        <span className="loppu pvm">{yearFromIsoDateString(modelTitle(opiskeluoikeus, 'päättymispäivä'))},</span>
                                      </span>
                                    : null
                                  }
                                  <span className="tila">{ modelTitle(opiskeluoikeus, 'tila.opiskeluoikeusjaksot.-1.tila') }</span>
                                </li>
                              )
                            )
                          }
                        </ul>
                      </li>
                    )
                  }
                </ul>
              </div>)
              return (
                <li className={className} key={tyyppiIndex}>
                  { selected ? content : <Link href={'?opiskeluoikeudenTyyppi=' + koodiarvo}>{content}</Link> }
                </li>)
            })}
          {
            canAddOpiskeluoikeusP.map( canAdd => canAdd && <li key="new" className="add-opiskeluoikeus">
                <span className="plus" onClick={toggleAdd}></span>
                <a onClick={toggleAdd}>Lisää opiskeluoikeus</a>
                {
                  addingAtom.map(adding => adding && <UusiOpiskeluoikeusPopup resultCallback={addOpiskeluoikeus}/>)
                }
              </li>
            )
          }
        </ul>

        <ul className="opiskeluoikeuksientiedot">
          {
            modelItems(model, 'opiskeluoikeudet.' + selectedIndex + '.opiskeluoikeudet').flatMap((oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) => {
              return modelItems(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) =>
                <li key={ oppilaitosIndex + '-' + opiskeluoikeusIndex }>
                  <OpiskeluoikeusEditor
                    model={ addContext(opiskeluoikeus, { oppijaOid: oppijaOid }) }
                  />
                </li>
              )
            })
          }
        </ul>
      </div>)
  }