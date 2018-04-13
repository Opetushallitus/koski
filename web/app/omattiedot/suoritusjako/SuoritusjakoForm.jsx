import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import Http from '../../util/http'
import Text from '../../i18n/Text'
import {SuoritusjakoLink} from './SuoritusjakoLink'
import {SelectableSuoritusList} from './SelectableSuoritusList'
import {ToggleButton} from '../../components/ToggleButton'

const Url = '/koski/api/suoritusjako'
const doShare = suoritusIds => Http.put(Url, [...suoritusIds])

const Ingressi = () => (
  <div className='suoritusjako-form__caption'>
    <Text name={
      'Luomalla jakolinkin voit näyttää suoritustietosi haluamillesi henkilöille (esimerkiksi työtä tai opiskelupaikkaa hakiessasi). ' +
      'Tiettyjä arkaluonteisia tietoja (muun muassa tietoa vammaisten opiskelijoiden tuesta) ei välitetä. ' +
      'Luotuasi linkin voit tarkistaa tarkan sisällön Esikatsele-painikkeella.'}
    />
  </div>
)

const SuoritusjakoList = ({opiskeluoikeudet, suoritusjaot}) => (
  <div>
    {!R.isEmpty(suoritusjaot) && <h2><Text name='Voimassaolevat linkit'/></h2>}

    <ul className='suoritusjako-form__link-list'>
      {suoritusjaot.map(suoritusjako => (
        <li key={suoritusjako.secret}>
          <SuoritusjakoLink suoritusjako={suoritusjako} opiskeluoikeudet={opiskeluoikeudet}/>
        </li>
      ))}
    </ul>
  </div>
)

const CreateNewSuoritusjakoButton = ({selectedSuoritusIds, onSuccess}) => (
  <div className='create-suoritusjako__button'>
    <button onClick={() => doShare(selectedSuoritusIds).onValue(onSuccess)}>
      <Text name='Jaa valitsemasi opinnot'/>
    </button>
  </div>
)

const NewSuoritusjako = ({opiskeluoikeudet, selectedSuoritusIds, onSuccess, showForm, canCancelForm}) => (
  <div className='suoritusjako-form__create-suoritusjako'>
    {showForm.map(show => show
      ? (
        <div>
          <div className='create-suoritusjako-header-row'>
            <h2><Text name='Valitse jaettavat suoritustiedot'/></h2>
            {canCancelForm.map(canCancel => canCancel && <ToggleButton toggleA={showForm} text='Peruuta' style='text'/>)}
          </div>
          <div className='create-suoritusjako'>
            <SelectableSuoritusList opiskeluoikeudet={opiskeluoikeudet} selectedSuoritusIds={selectedSuoritusIds}/>
            <CreateNewSuoritusjakoButton baret-lift selectedSuoritusIds={selectedSuoritusIds} onSuccess={onSuccess}/>
          </div>
        </div>
      )
      : <ToggleButton toggleA={showForm} text='Luo uusi' style='text'/>
    )}
  </div>
)

export class SuoritusjakoForm extends React.Component {
  constructor(props) {
    super(props)

    this.selectedSuoritusIds = Atom([])
    this.suoritusjaot = Atom([])

    this.showNewSuoritusjakoForm = Atom(false)
    this.canCancelForm = this.suoritusjaot.map(R.complement(R.isEmpty))
  }

  componentDidMount() {
    this.suoritusjaot.onValue(suoritusjaot => this.showNewSuoritusjakoForm.set(R.isEmpty(suoritusjaot)))
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
        {this.suoritusjaot.map(suoritusjaot => R.isEmpty(suoritusjaot) && <Ingressi/>)}
        <SuoritusjakoList baret-lift opiskeluoikeudet={opiskeluoikeudet} suoritusjaot={this.suoritusjaot}/>
        <NewSuoritusjako
          opiskeluoikeudet={opiskeluoikeudet}
          selectedSuoritusIds={this.selectedSuoritusIds}
          onSuccess={this.addLink.bind(this)}
          showForm={this.showNewSuoritusjakoForm}
          canCancelForm={this.canCancelForm}
        />
      </section>
    )
  }
}
