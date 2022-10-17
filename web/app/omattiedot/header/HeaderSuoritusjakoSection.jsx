import React from 'baret'
import { Popup } from '../../components/Popup'
import { SuoritusjakoForm } from '../suoritusjako/SuoritusjakoForm'
import { FormState } from './Header'

export const HeaderSuoritusjakoSection = ({ uiModeA, opiskeluoikeudet }) => (
  <section
    className="suoritusjako"
    data-show={uiModeA.map((mode) => mode === FormState.SUORITUSJAKO)}
  >
    <Popup
      showStateAtom={uiModeA}
      dismissedStateValue={FormState.NONE}
      onFocusValue={FormState.SUORITUSJAKO}
      inline={true}
      closeTitle="Suoritustietojen jakaminen"
    >
      <SuoritusjakoForm
        opiskeluoikeudet={opiskeluoikeudet}
        showFormAtom={uiModeA}
      />
    </Popup>
  </section>
)
