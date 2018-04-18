import React from 'baret'
import {ift} from '../../util/util'
import Text from '../../i18n/Text'
import {TiedotPalvelussa} from '../TiedotPalvelussa'
import {Popup} from '../../components/Popup'
import {ToggleButton} from '../../components/ToggleButton'

export const HeaderInfo = ({showPalvelussaNäkyvätTiedotA}) => (
  <div className='header__info'>
    <h1 className='header__heading'>
      <Text name='Opintoni'/>
    </h1>
    <div className='header__caption'>
      <p>
        <Text name='Opintoni ingressi'/>
      </p>
      <div className='header__info-toggle'>
        <ToggleButton
          toggleA={showPalvelussaNäkyvätTiedotA}
          text='Mitkä tiedot palvelussa näkyvät?'
          style='text'
        />
      </div>
    </div>

    {ift(showPalvelussaNäkyvätTiedotA,
      <Popup showStateAtom={showPalvelussaNäkyvätTiedotA} inline={true}>
        <TiedotPalvelussa/>
      </Popup>
    )}
  </div>
)
