import React from 'baret'
import {Popup} from '../../components/Popup'
import {RaportoiVirheestäForm} from '../virheraportointi/RaportoiVirheestaForm'
import {FormState} from './Header'

export const HeaderVirheraportointiSection = ({uiModeA, henkilöP, opiskeluoikeudetP}) => (
  <section className='virheraportointi' data-show={uiModeA.map(mode => mode === FormState.VIRHERAPORTOINTI)}>
    <Popup showStateAtom={uiModeA} dismissedStateValue={FormState.NONE} onFocusValue={FormState.VIRHERAPORTOINTI} inline={true} closeTitle='Onko suorituksissasi virhe?'>
      {opiskeluoikeudetP.map(opiskeluoikeudet => (<RaportoiVirheestäForm
        henkilöP={henkilöP}
        opiskeluoikeudet={opiskeluoikeudet}
      />))}
    </Popup>
  </section>
)
