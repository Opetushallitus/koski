import React from 'baret'
import Atom from 'bacon.atom'
import {onLopputilassa} from '../editor/OpiskeluoikeudenTilaEditor.jsx'
import UusiPerusopetuksenVuosiluokanSuoritus from './UusiPerusopetuksenVuosiluokanSuoritus.jsx'
import UusiPerusopetuksenOppiaineenOppimääränSuoritus from './UusiPerusopetuksenOppiaineenOppimaaranSuoritus.jsx'
import UusiAikuistenPerusopetuksenOppimaaranSuoritus from './UusiAikuistenPerusopetuksenOppimaaranSuoritus.jsx'
import UusiAikuistenPerusopetuksenAlkuvaiheenSuoritus from './UusiAikuistenPerusopetuksenAlkuvaiheenSuoritus.jsx'

export default ({opiskeluoikeus, callback}) => {
  return (<span className="add-suoritus tab">{
    opiskeluoikeus.context.edit && !onLopputilassa(opiskeluoikeus) && findPopUps(opiskeluoikeus).map((PopUp, i) => {
      let addingAtom = Atom(false)
      let resultCallback = (suoritus) => {
        if (suoritus) {
          callback(suoritus)
        } else {
          addingAtom.set(false)
        }
      }

      let startAdding = () => {
        if (PopUp.createSuoritus) {
          callback(PopUp.createSuoritus(opiskeluoikeus))
        } else {
          addingAtom.modify(x => !x)
        }
      }

      return (<span key={i}>
        {
          opiskeluoikeus.context.edit && !onLopputilassa(opiskeluoikeus) && (
            <a className="add-suoritus-link" onClick={startAdding}><span className="plus">{''}</span>{PopUp.addSuoritusTitle(opiskeluoikeus)}</a>
          )
        }
        {
          addingAtom.map(adding => adding && <PopUp {...{opiskeluoikeus, resultCallback}}/>)
        }
      </span>)
    })
  }</span>)
}

const popups = [
  UusiPerusopetuksenOppiaineenOppimääränSuoritus,
  UusiPerusopetuksenVuosiluokanSuoritus,
  UusiAikuistenPerusopetuksenOppimaaranSuoritus,
  UusiAikuistenPerusopetuksenAlkuvaiheenSuoritus
]

const findPopUps = (opiskeluoikeus) => popups.filter(popup => popup.canAddSuoritus(opiskeluoikeus))