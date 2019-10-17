import React from 'baret'
import {modelData, modelTitle} from '../../editor/EditorModel'
import {ISO2FinnishDate} from '../../date/date'
import Text from '../../i18n/Text'

export const HeaderName = ({henkilöP}) => {
  const nimiP = henkilöP.map(henkilö => <p className='textstyle-like-h2'>{`${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`}</p>)
  const syntymäaikaP = henkilöP.map(henkilö => modelTitle(henkilö, 'syntymäaika') &&
    <p className='syntynyt textstyle-lead'>
      <Text name='syntynyt'/>
      <span> {ISO2FinnishDate(modelTitle(henkilö, 'syntymäaika'))}</span>
    </p>)

  return (
    <div className='header__name'>
      {nimiP}
      {syntymäaikaP}
    </div>
  )
}
