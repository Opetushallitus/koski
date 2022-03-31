import React from 'baret'
import {Popup} from '../../components/Popup'
import {FormState} from './Header'
import Text from '../../i18n/Text'

const valitseHuollettava = () => window.location = '/koski/huoltaja/valitse'

export const HeaderHuollettavanTiedotSection = ({uiModeA}) => (
  <section className='huollettavan-tiedot textstyle-body' data-show={uiModeA.map(mode => mode === FormState.HUOLLETTAVANTIEDOT)}>
    <Popup showStateAtom={uiModeA} dismissedStateValue={FormState.NONE} onFocusValue={FormState.HUOLLETTAVANTIEDOT} inline={true} closeTitle='Suoritustietojen jakaminen'>
      <div className='huollettavan-tiedot-info'>
        <Text name='Jos sinulla on huollettavia, voit tarkastella myös heidän opintotietojaan...' />
      </div>
      <a id='valitse-huollettava' className='koski-button' onClick={valitseHuollettava}><Text name='Tarkastele huollettavasi tietoja'/></a>
    </Popup>
  </section>
)

HeaderHuollettavanTiedotSection.displayName = 'HeaderHuollettavanTiedotSection'

