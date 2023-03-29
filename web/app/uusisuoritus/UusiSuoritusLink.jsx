import React from 'baret'
import Atom from 'bacon.atom'
import { onLopputilassa } from '../opiskeluoikeus/OpiskeluoikeudenTilaEditor'
import UusiPerusopetuksenVuosiluokanSuoritus from './UusiPerusopetuksenVuosiluokanSuoritus'
import UusiPerusopetuksenOppiaineenOppimääränSuoritus from './UusiPerusopetuksenOppiaineenOppimaaranSuoritus'
import UusiAikuistenPerusopetuksenOppimaaranSuoritus from './UusiAikuistenPerusopetuksenOppimaaranSuoritus'
import UusiAikuistenPerusopetuksenAlkuvaiheenSuoritus from './UusiAikuistenPerusopetuksenAlkuvaiheenSuoritus'
import {
  UusiAmmatillisenTutkinnonSuoritus,
  UusiNäyttötutkintoonValmistavanKoulutuksenSuoritus
} from './UusiAmmatillisenTutkinnonSuoritus'
import {
  UusiIBTutkinnonSuoritus,
  UusiPreIBSuoritus
} from './UusiIBTutkinnonSuoritus'
import {
  UusiDIATutkinnonSuoritus,
  UusiValmistavanDIAVaiheenSuoritus
} from './UusiDIATutkinnonSuoritus'
import { UusiInternationalSchoolVuosiluokanSuoritus } from './UusiInternationalSchoolVuosiluokanSuoritus'
import { UusiEuropeanSchoolOfHelsinkiVuosiluokanSuoritus } from './UusiEuropeanSchoolOfHelsinkiVuosiluokanSuoritus'
import { t } from '../i18n/i18n'

export default ({ opiskeluoikeus, callback }) => {
  return (
    <span className="add-suoritus tab">
      {opiskeluoikeus.context.edit &&
        !onLopputilassa(opiskeluoikeus) &&
        findPopUps(opiskeluoikeus).map((PopUp, i) => {
          const addingAtom = Atom(false)
          const resultCallback = (suoritus) => {
            if (suoritus) {
              callback(suoritus)
            } else {
              addingAtom.set(false)
            }
          }

          const startAdding = () => {
            if (PopUp.createSuoritus) {
              PopUp.createSuoritus(opiskeluoikeus).onValue(callback)
            } else {
              addingAtom.modify((x) => !x)
            }
          }

          return (
            <span key={i}>
              {opiskeluoikeus.context.edit &&
                !onLopputilassa(opiskeluoikeus) && (
                  <a
                    className="add-suoritus-link"
                    onClick={startAdding}
                    role="button"
                    tabIndex={0}
                    aria-label={
                      PopUp.addSuoritusTitleKey
                        ? t(PopUp.addSuoritusTitleKey)
                        : t('lisää suoritus')
                    }
                  >
                    <span className="plus">{''}</span>
                    {PopUp.addSuoritusTitle(opiskeluoikeus)}
                  </a>
                )}
              {addingAtom.map(
                (adding) =>
                  adding && <PopUp {...{ opiskeluoikeus, resultCallback }} />
              )}
            </span>
          )
        })}
    </span>
  )
}

const popups = [
  UusiPerusopetuksenOppiaineenOppimääränSuoritus,
  UusiPerusopetuksenVuosiluokanSuoritus,
  UusiAikuistenPerusopetuksenOppimaaranSuoritus,
  UusiAikuistenPerusopetuksenAlkuvaiheenSuoritus,
  UusiAmmatillisenTutkinnonSuoritus,
  UusiNäyttötutkintoonValmistavanKoulutuksenSuoritus,
  UusiIBTutkinnonSuoritus,
  UusiPreIBSuoritus,
  UusiDIATutkinnonSuoritus,
  UusiValmistavanDIAVaiheenSuoritus,
  UusiInternationalSchoolVuosiluokanSuoritus,
  UusiEuropeanSchoolOfHelsinkiVuosiluokanSuoritus
]

const findPopUps = (opiskeluoikeus) =>
  popups.filter((popup) => popup.canAddSuoritus(opiskeluoikeus))
