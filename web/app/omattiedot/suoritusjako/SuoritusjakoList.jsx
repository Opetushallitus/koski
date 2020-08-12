import React, {fromBacon} from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../../i18n/Text'
import Http from '../../util/http'
import {SuoritusjakoLink} from './SuoritusjakoLink'

const url = '/koski/api/suoritusjakoV2/available'

class SuoritusjakoList extends React.Component {
  constructor(props) {
    super(props)
    this.updateSuoritusjaot = this.updateSuoritusjaot.bind(this)

    this.suoritusjaot = Atom([])
  }

  updateSuoritusjaot() {
    Http.get(
      url,
      {
        errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error(e)
      }
    ).onValue(suoritusjaot => this.suoritusjaot.set(suoritusjaot))
  }

  componentDidMount() {
    this.updateSuoritusjaot()
  }

  render() {
    return fromBacon(this.suoritusjaot.map(suoritusjaot => (
      <>
        <p className='textstyle-like-h2'>{suoritusjaot.length} <Text
          name='voimassaolevaa linkkiä'/></p>
        <p><Text name=''/></p>
        <div>
          {suoritusjaot.length > 0
            ? suoritusjaot.map(suoritusjako => (
              <SuoritusjakoLink
                baret-lift
                key={suoritusjako.secret}
                suoritusjako={suoritusjako}
                onRemove={this.updateSuoritusjaot}
              />
            ))
            : <Text name='Ei jakolinkkejä'/>}
        </div>
      </>
    )))
  }
}

export default SuoritusjakoList
