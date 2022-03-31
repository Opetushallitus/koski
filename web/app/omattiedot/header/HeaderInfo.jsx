import React from 'baret'
import Text from '../../i18n/Text'
import {Varoitukset} from '../../util/Varoitukset'
import {hasOpintoja} from '../../OmatTiedot'

export const HeaderInfo = ({oppija, varoitukset}) => (
  <div className='header__info'>
    <Varoitukset varoitukset={varoitukset}/>
    <h1 className='header__heading'>
      {oppija.context.huollettava ? <Text name='Huollettavani opinnot'/> : <Text name='Opintoni'/>}
    </h1>
    {hasOpintoja(oppija) && (<div className='header__caption'>
      <p className='textstyle-lead'>
        <Text name='Opintoni ingressi'/>
      </p>
    </div>)}
  </div>
)

HeaderInfo.displayName = 'HeaderInfo'
