import React from 'baret'
import Text from '../../i18n/Text'
import {Varoitukset} from '../../util/Varoitukset'
import {ifte} from '../../util/util'
import {isHuoltaja} from './Header'

export const HeaderInfo = ({varoituksetP, oppijaP}) => (
  <div className='header__info'>
    {varoituksetP.map(varoitukset => <Varoitukset varoitukset={varoitukset}/>)}
    <h1 className='header__heading'>
      {ifte(oppijaP.map(isHuoltaja), <Text name='Opintoni'/>, <Text name='Huollettavani opinnot'/>)}
    </h1>
    <div className='header__caption'>
      <p className='textstyle-lead'>
        <Text name='Opintoni ingressi'/>
      </p>
    </div>
  </div>
)
