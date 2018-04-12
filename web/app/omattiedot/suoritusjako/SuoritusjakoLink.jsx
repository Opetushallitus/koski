import React from 'react'
import {CopyableText} from '../../components/CopyableText'
import Text from '../../i18n/Text'
import DateInput from '../../date/DateInput'
import {parseISODate} from '../../date/date'

export const SuoritusjakoLink = ({suoritusjako}) => {
  const {secret, expirationDate} = suoritusjako
  const url = `${window.location.origin}/koski/opinnot/${secret}`

  return (
    <div className='suoritusjako-link'>
      <div className='suoritusjako-link__top-container'>
        <CopyableText className='suoritusjako-link__url' message={url} multiline={false}/>
        <div className='suoritusjako-link__expiration'>
          <label>
            <Text name='Linkin voimassaoloaika'/>
            <Text name='Päättyy'/>
            <DateInput value={parseISODate(expirationDate)}/>
          </label>
        </div>
      </div>
      <div className='suoritusjako-link__bottom-container'>
        <div className='suoritusjako-link__preview'>
          <a target='_blank' href={url}><Text name='Esikatsele'/></a>
        </div>
        <div className='suoritusjako-link__remove'>
          <a href=''><Text name='Poista linkki käytöstä'/></a>
        </div>
      </div>
    </div>
  )
}
