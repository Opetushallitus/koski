import React from 'baret'
import Text from '../../i18n/Text'
import {Varoitukset} from '../../util/Varoitukset'
import Link from '../../components/Link'
import {hasOpintoja} from '../../OmatTiedot'

export const HeaderInfo = ({oppija, varoitukset}) => (
  <div className='header__info'>
    <Varoitukset varoitukset={varoitukset}/>
    {oppija.context.huollettava && <Link className='palaa-omiin-tietoihin' href='/koski/omattiedot'><Text name='Palaa omiin opintotietoihin'/></Link>}
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
