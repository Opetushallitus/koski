import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import Http from '../../util/http'
import Text from '../../i18n/Text'
import {SuoritusjakoLink} from './SuoritusjakoLink'
import {SelectableSuoritusList} from './SelectableSuoritusList'

const Url = '/koski/api/suoritusjako'
const doShare = suoritusIds => Http.put(Url, [...suoritusIds])

const Ingressi = () => (
  <div className='suoritusjako-form__caption'>
    <Text name={
      'Luomalla jakolinkin voit näyttää suoritustietosi haluamillesi henkilöille (esimerkiksi työtä tai opiskelupaikkaa hakiessasi). ' +
      'Henkilötietoja tai muita arkaluontoisia tietoja ei jaeta.'}
    />
  </div>
)

const SuoritusjakoList = ({opiskeluoikeudet, suoritusjaot}) => (
  <ul className='suoritusjako-form__link-list'>
    {suoritusjaot.map(suoritusjako => (
      <li key={suoritusjako.secret}>
        <SuoritusjakoLink suoritusjako={suoritusjako} opiskeluoikeudet={opiskeluoikeudet}/>
      </li>
    ))}
  </ul>
)

const NewSuoritusjakoButton = ({selectedSuoritusIds, onSuccess}) => (
  <div className='create-suoritusjako__button'>
    <button onClick={() => doShare(selectedSuoritusIds).onValue(onSuccess)}>
      <Text name='Jaa valitsemasi opinnot'/>
    </button>
  </div>
)

const NewSuoritusjako = ({opiskeluoikeudet, selectedSuoritusIds, onSuccess}) => (
  <div className='suoritusjako-form__create-suoritusjako'>
    <h2><Text name='Valitse jaettavat suoritustiedot'/></h2>
    <div className='create-suoritusjako'>
      <SelectableSuoritusList opiskeluoikeudet={opiskeluoikeudet} selectedSuoritusIds={selectedSuoritusIds}/>
      <NewSuoritusjakoButton baret-lift selectedSuoritusIds={selectedSuoritusIds} onSuccess={onSuccess}/>
    </div>
  </div>
)

export class SuoritusjakoForm extends React.Component {
  constructor(props) {
    super(props)

    this.selectedSuoritusIds = Atom([])
    this.suoritusjaot = Atom([])
  }

  componentDidMount() {
    Http.get(Url).onValue(jaot => this.suoritusjaot.set(jaot))
  }

  addLink(suoritusjako) {
    this.suoritusjaot.modify(list => R.append(suoritusjako, list))
    this.selectedSuoritusIds.set([])
  }

  render() {
    const {opiskeluoikeudet} = this.props

    return (
      <section className='suoritusjako-form'>
        <Ingressi/>
        <SuoritusjakoList baret-lift opiskeluoikeudet={opiskeluoikeudet} suoritusjaot={this.suoritusjaot}/>
        <NewSuoritusjako
          opiskeluoikeudet={opiskeluoikeudet}
          selectedSuoritusIds={this.selectedSuoritusIds}
          onSuccess={this.addLink.bind(this)}
        />
      </section>
    )
  }
}
