import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as R from 'ramda'
import Http from '../../util/http'
import Text from '../../i18n/Text'
import {SuoritusjakoLink, SuoritusjakoLinkPlaceholder} from './SuoritusjakoLink'
import {SelectableSuoritusList} from './SelectableSuoritusList'
import {ToggleButton} from '../../components/ToggleButton'

const Url = '/koski/api/suoritusjako'
const doShare = suoritusIds => Http.post(Url, [...suoritusIds])

const Ingressi = () => (
  <div className='suoritusjako-form__caption'>
    <Text name={
      'Luomalla jakolinkin voit näyttää suoritustietosi haluamillesi henkilöille (esimerkiksi työtä tai opiskelupaikkaa hakiessasi). ' +
      'Luotuasi linkin voit tarkistaa tarkan sisällön Esikatsele-painikkeella.'
    }
    />
  </div>
)

const SuoritusjakoList = ({opiskeluoikeudet, suoritusjaot, onRemove}) => (
  <div>
    {!R.isEmpty(suoritusjaot) && <h2 className="link-successful-h2"><div className="link-successful-icon"/><Text name="Jakolinkin luominen onnistui."/></h2>}
    {!R.isEmpty(suoritusjaot) && <h2><Text name='Voimassaolevat linkit'/></h2>}

    <ul className='suoritusjako-form__link-list'>
      {suoritusjaot.map(suoritusjako => (
        <li key={suoritusjako.secret}>
          <SuoritusjakoLink suoritusjako={suoritusjako} opiskeluoikeudet={opiskeluoikeudet} onRemove={onRemove}/>
        </li>
      ))}
    </ul>
  </div>
)

const CreateNewSuoritusjakoButton = ({selectedSuoritusIds, onClick, onSuccess, onError}) => {
  const clickAction = () => {
    onClick()

    const res = doShare(selectedSuoritusIds)
    res.onValue(onSuccess)
    res.onError(onError)
  }

  return (
    <div className='create-suoritusjako__button'>
      <button className='koski-button' disabled={R.isEmpty(selectedSuoritusIds)} onClick={clickAction}>
        <Text name='Jaa valitsemasi opinnot'/>
      </button>
    </div>
  )
}

const NewSuoritusjako = ({opiskeluoikeudet, selectedSuoritusIds, onSuccess, showForm, canCancelForm}) => {
  const isPending = Atom(false)

  return (
    <div>
      {Bacon.combineWith(showForm, isPending, (form, pending) =>
        form ?
          pending ? <SuoritusjakoLinkPlaceholder transition='enter'/>
            : (
              <div className='suoritusjako-form__create-suoritusjako'>
                <div>
                  <div className='create-suoritusjako-header-row'>
                    <h2><Text name='Valitse jaettavat suoritustiedot'/></h2>
                    {canCancelForm.map(canCancel => canCancel && <ToggleButton toggleA={showForm} text='Peruuta' style='text'/>)}
                  </div>
                  <div className='create-suoritusjako'>
                    <SelectableSuoritusList opiskeluoikeudet={opiskeluoikeudet} selectedSuoritusIds={selectedSuoritusIds}/>
                    <CreateNewSuoritusjakoButton
                      baret-lift
                      selectedSuoritusIds={selectedSuoritusIds}
                      onClick={() => isPending.set(true)}
                      onSuccess={res => {
                        isPending.set(false)
                        onSuccess(res)
                      }}
                      onError={() => isPending.set(false)}
                    />
                  </div>
                </div>
              </div>
            )
          : <ToggleButton toggleA={showForm} text='Luo uusi' style='text'/>
      )}
    </div>
  )
}

export class SuoritusjakoForm extends React.Component {
  constructor(props) {
    super(props)

    this.selectedSuoritusIds = Atom([])
    this.suoritusjaot = Atom([])

    this.showNewSuoritusjakoForm = Atom(false)
    this.canCancelForm = this.suoritusjaot.map(R.complement(R.isEmpty))
  }

  componentDidMount() {
    this.suoritusjaot.slidingWindow(2).onValue(([prev, now]) => {
      if (!prev) return

      if (!now) this.showNewSuoritusjakoForm.set(R.isEmpty(prev))
      else if (R.isEmpty(now)) this.showNewSuoritusjakoForm.set(true)
      else if (now.length > prev.length) this.showNewSuoritusjakoForm.set(false)
    })

    Http.get(Url).onValue(jaot => this.suoritusjaot.set(jaot))
  }

  addLink(suoritusjako) {
    this.suoritusjaot.modify(list => R.append(suoritusjako, list))
    this.selectedSuoritusIds.set([])
  }

  removeLink(suoritusjako) {
    this.suoritusjaot.modify(list => R.without([suoritusjako], list))
  }

  render() {
    const {opiskeluoikeudet} = this.props

    return (
      <section className='suoritusjako-form textstyle-body'>
        {this.suoritusjaot.map(suoritusjaot => R.isEmpty(suoritusjaot) && <Ingressi/>)}
        <SuoritusjakoList
          baret-lift
          opiskeluoikeudet={opiskeluoikeudet}
          suoritusjaot={this.suoritusjaot}
          onRemove={this.removeLink.bind(this)}
        />
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
