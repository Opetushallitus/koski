import React from 'baret'
import Atom from 'bacon.atom'
import {onLopputilassa} from '../editor/OpiskeluoikeudenTilaEditor.jsx'
import UusiPerusopetuksenVuosiluokanSuoritusPopup from './UusiPerusopetuksenVuosiluokanSuoritusPopup.jsx'
import UusiPerusopetuksenOppiaineenSuoritusPopup from './UusiPerusopetuksenOppiaineenSuoritusPopup.jsx'

export default ({model, callback}) => {
  let addingAtom = Atom(false)
  let uusiSuoritusCallback = (suoritus) => {
    if (suoritus) {
      callback(suoritus)
    } else {
      addingAtom.set(false)
    }
  }

  return (<span className="add-suoritus tab">
    {
      model.context.edit && !onLopputilassa(model) && canAddSuoritus(model) && (
        <a className="add-suoritus-link" onClick={() => { addingAtom.modify(x => !x) }}><span className="plus">{''}</span>{addSuoritusTitle(model)}</a>
      )
    }
    {
      addingAtom.map(adding => adding && <UusiSuoritusPopup opiskeluoikeus={model} resultCallback={uusiSuoritusCallback}/>)
    }
  </span>)
}


const UusiSuoritusPopup = ({opiskeluoikeus, resultCallback}) => {
  let PopUp = findPopUp(opiskeluoikeus)
  return <PopUp {...{opiskeluoikeus, resultCallback}}/>
}

const canAddSuoritus = (opiskeluoikeus) => !!findPopUp(opiskeluoikeus)
const addSuoritusTitle = (opiskeluoikeus) => findPopUp(opiskeluoikeus).addSuoritusTitle(opiskeluoikeus)

const popups = [UusiPerusopetuksenOppiaineenSuoritusPopup, UusiPerusopetuksenVuosiluokanSuoritusPopup]
const findPopUp = (opiskeluoikeus) => popups.find(popup => popup.canAddSuoritus(opiskeluoikeus))

/*
const UusiAikuistenPerusopetuksenPäättövaiheenOpintojenSuoritus = ({}) => {

}
*/