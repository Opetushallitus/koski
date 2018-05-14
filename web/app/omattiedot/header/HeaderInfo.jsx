import React from 'baret'
import Text from '../../i18n/Text'
import {Varoitukset} from '../../util/Varoitukset'

export const HeaderInfo = ({varoitukset}) => (
  <div className='header__info'>
    <Varoitukset varoitukset={varoitukset}/>
    <h1 className='header__heading'>
      <Text name='Opintoni'/>
    </h1>
    <div className='header__caption'>
      <p className='textstyle-lead'>
        <Text name='Opintoni ingressi'/>
      </p>
    </div>
  </div>
)
