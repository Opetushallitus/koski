import React from 'baret'
import UusiPerusopetuksenVuosiluokanSuoritusPopup from './UusiPerusopetuksenVuosiluokanSuoritusPopup.jsx'
import UusiPerusopetuksenOppiaineenSuoritusPopup from './UusiPerusopetuksenOppiaineenSuoritusPopup.jsx'

const UusiSuoritusPopup = ({opiskeluoikeus, resultCallback}) => {
  let PopUp = findPopUp(opiskeluoikeus)
  return <PopUp {...{opiskeluoikeus, resultCallback}}/>
}

UusiSuoritusPopup.canAddSuoritus = (opiskeluoikeus) => !!findPopUp(opiskeluoikeus)
UusiSuoritusPopup.addSuoritusTitle = (opiskeluoikeus) => findPopUp(opiskeluoikeus).addSuoritusTitle(opiskeluoikeus)

const popups = [UusiPerusopetuksenOppiaineenSuoritusPopup, UusiPerusopetuksenVuosiluokanSuoritusPopup]
const findPopUp = (opiskeluoikeus) => popups.find(popup => popup.canAddSuoritus(opiskeluoikeus))

export default UusiSuoritusPopup