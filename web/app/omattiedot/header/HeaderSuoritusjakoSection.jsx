import React from 'baret'
import {Popup} from '../../components/Popup'
import {SuoritusjakoForm} from '../suoritusjako/SuoritusjakoForm'

export const HeaderSuoritusjakoSection = ({showSuoritusjakoA}) => (
  <section className='suoritusjako'>
    <Popup showStateAtom={showSuoritusjakoA} inline={true}>
      <SuoritusjakoForm/>
    </Popup>
  </section>
)
