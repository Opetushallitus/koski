import React from 'react'
import {CopyableText} from '../../components/CopyableText'
import Text from '../../i18n/Text'
import DateInput from '../../date/DateInput'
import {formatISODate, parseISODate} from '../../date/date'
import Http from '../../util/http'

const ApiBaseUrl = '/koski/api/suoritusjako'

const doDelete = secret => Http.post(`${ApiBaseUrl}/delete`, {secret})
const doUpdate = (secret, expirationDate) => Http.post(`${ApiBaseUrl}/update`, {secret, expirationDate})

export const SuoritusjakoLink = ({suoritusjako, onRemove}) => {
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
            <DateInput
              value={parseISODate(expirationDate)}
              valueCallback={date => doUpdate(secret, formatISODate(date))}
            />
          </label>
        </div>
      </div>
      <div className='suoritusjako-link__bottom-container'>
        <div className='suoritusjako-link__preview'>
          <a target='_blank' href={url}><Text name='Esikatsele'/></a>
        </div>
        <div className='suoritusjako-link__remove'>
          <a onClick={() => doDelete(secret).onValue(() => onRemove(suoritusjako))}>
            <Text name='Poista linkki käytöstä'/>
          </a>
        </div>
      </div>
    </div>
  )
}
