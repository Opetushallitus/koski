import React from 'baret'
import Text from '../../i18n/Text'
import {Varoitukset} from '../../util/Varoitukset'
import {ifte} from '../../util/util'

export const HeaderInfo = ({varoitukset, oppijaP}) => (
  <div className='header__info'>
    <Varoitukset varoitukset={varoitukset}/>
    <h1 className='header__heading'>
      {ifte(oppijaP.map(o => o && o.isHuollettava), <Text name='Huollettavani opinnot'/>, <Text name='Opintoni'/>)}
    </h1>
    <div className='header__caption'>
      <p className='textstyle-lead'>
        <Text name='Opintoni ingressi'/>
      </p>
    </div>
  </div>
)
