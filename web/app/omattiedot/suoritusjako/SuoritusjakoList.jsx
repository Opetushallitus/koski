import React, {fromBacon} from 'baret'
import Bacon from 'baconjs'
import Text from '../../i18n/Text'
import Http from '../../util/http'
import {SuoritusjakoLink} from './SuoritusjakoLink'

const suoritusjaotP = () => {
  const url = '/koski/api/suoritusjakoV2/available'
  return Http.cachedGet(
    url,
    {
      errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error(e)
    }
  ).toProperty()
}

const SuoritusjakoList = () => {
  return fromBacon(suoritusjaotP().map(suoritusjaot => (
    <>
      <p className='textstyle-like-h2'>{suoritusjaot.length} <Text name='voimassaolevaa linkkiä'/></p>
      <p><Text name=''/></p>
      <div>
        {suoritusjaot.length > 0
          ? suoritusjaot.map((suoritusjako, i) => <SuoritusjakoLink key={i} suoritusjako={suoritusjako}/>)
          : <Text name='Ei jakolinkkejä'/>}
      </div>
    </>
  )))
}

export default SuoritusjakoList
