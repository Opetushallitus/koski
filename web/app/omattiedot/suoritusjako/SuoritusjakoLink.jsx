import React from 'react'
import {CopyableText} from '../../components/CopyableText'
import Text from '../../i18n/Text'
import DateInput from '../../date/DateInput'

export const SuoritusjakoLink = ({secret}) => {
  return (
    <div className='suoritusjako-link'>
      <div className='suoritusjako-link__top-container'>
        <CopyableText className='suoritusjako-link__url' message={secret} multiline={false}/>
        <a className='suoritusjako-link__preview' href=''><Text name='Esikatsele'/></a>
        <div className='suoritusjako-link__expiration'>
          <label>
            <Text name='Linkin voimassaoloaika'/>
            <Text name='Päättyy'/>
            <DateInput/>
          </label>
        </div>
      </div>
      <div className='suoritusjako-link__bottom-container'>
        <div className='suoritusjako-link__suoritukset'>
          <h3><Text name='Sisältää seuraavat tiedot:'/></h3>
          <ul>
          </ul>
        </div>
        <div className='suoritusjako-link__remove'>
          <a href=''><Text name='Poista linkki käytöstä'/></a>
        </div>
      </div>
    </div>
  )
}
