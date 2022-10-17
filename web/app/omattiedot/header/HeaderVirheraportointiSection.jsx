import React from 'baret'
import { Popup } from '../../components/Popup'
import { RaportoiVirheestäForm } from '../virheraportointi/RaportoiVirheestaForm'
import { FormState } from './Header'
import { modelItems, modelLookup } from '../../editor/EditorModel'

export const HeaderVirheraportointiSection = ({ uiModeA, oppija }) => (
  <section
    className="virheraportointi"
    data-show={uiModeA.map((mode) => mode === FormState.VIRHERAPORTOINTI)}
  >
    <Popup
      showStateAtom={uiModeA}
      dismissedStateValue={FormState.NONE}
      onFocusValue={FormState.VIRHERAPORTOINTI}
      inline={true}
      closeTitle={virheRaportointiTitle(oppija)}
    >
      <RaportoiVirheestäForm
        henkilö={modelLookup(oppija, 'henkilö')}
        opiskeluoikeudet={modelItems(oppija, 'opiskeluoikeudet')}
      />
    </Popup>
  </section>
)

export const virheRaportointiTitle = (oppija) => {
  return oppija.context.huollettava
    ? 'Onko suorituksissa virhe'
    : 'Onko suorituksissasi virhe?'
}
