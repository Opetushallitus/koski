import React, {fromBacon} from 'baret'
import Bacon from 'baconjs'
import Text from '../../i18n/Text'
import Http from '../../util/http'

const suoritusjaotP = () => {
  const url = '/koski/api/suoritusjakoV2/available'
  return Http.cachedGet(
    url,
    {
      errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error(e)
    }
  ).toProperty()
}


const Suoritusjako = ({ suoritusjako }) => (
  <>
    <h2>Jakolinkki</h2>
    <p>Luotu {suoritusjako.timestamp}</p>
    <p>Vanhenee {suoritusjako.expirationDate}</p>
  </>
)

const SuoritusjakoList = () => {
  return fromBacon(suoritusjaotP().map(suoritusjaot => (
    <div>
      {suoritusjaot.length > 0
        ? suoritusjaot.map((suoritusjako, i) => <Suoritusjako key={i} suoritusjako={suoritusjako}/>)
        : <Text name='Ei jakolinkkejä'/>}
    </div>
  )))
}

export default SuoritusjakoList
