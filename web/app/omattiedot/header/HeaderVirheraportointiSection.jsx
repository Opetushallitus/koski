import React from 'baret'
import {Popup} from '../../components/Popup'
import {RaportoiVirheestäForm} from '../virheraportointi/RaportoiVirheestaForm'

export const HeaderVirheraportointiSection = ({showVirheraportointiA, henkilö, opiskeluoikeudet}) =>
  <section className='virheraportointi'>
    <Popup showStateAtom={showVirheraportointiA} inline={true}>
      <RaportoiVirheestäForm
        henkilö={henkilö}
        opiskeluoikeudet={opiskeluoikeudet}
      />
    </Popup>
  </section>
