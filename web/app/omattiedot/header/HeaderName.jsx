import React from 'react'
import {modelData, modelTitle} from '../../editor/EditorModel'
import {ISO2FinnishDate} from '../../date/date'
import Text from '../../i18n/Text'

export const HeaderName = ({henkilö}) => {
  const nimi = <p className='textstyle-like-h2'>{`${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`}</p>
  const syntymäaika = modelTitle(henkilö, 'syntymäaika') &&
    <p className='syntynyt textstyle-lead'>
      <Text name='syntynyt'/>
      <span> {ISO2FinnishDate(modelTitle(henkilö, 'syntymäaika'))}</span>
    </p>


  return (
    <div className='header__name'>
      {nimi}
      {syntymäaika}
    </div>
  )
}

HeaderName.displayName = 'HeaderName'
