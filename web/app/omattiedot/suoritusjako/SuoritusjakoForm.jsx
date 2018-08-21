import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as R from 'ramda'
import Http from '../../util/http'
import Text from '../../i18n/Text'
import {SuoritusjakoLink, SuoritusjakoLinkPlaceholder} from './SuoritusjakoLink'
import {SelectableSuoritusList} from './SelectableSuoritusList'
import {ToggleButton} from '../../components/ToggleButton'
import {focusWithoutScrolling} from '../../util/util'

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
    {!R.isEmpty(suoritusjaot) && (
      <div>
        <h2><Text name='Voimassaolevat linkit'/></h2>
        <div className="link-information">
          <Text name={
            'Jakolinkillä voit näyttää suoritustietosi haluamillesi henkilöille (esimerkiksi työtä tai opiskelupaikkaa hakiessasi). ' +
            'Linkin saajan ei tarvitse kirjautua Oma Opintopolku-palveluun.'}
          />
        </div>
        <ul className='suoritusjako-form__link-list'>
          {suoritusjaot.map(suoritusjako => (
            <li key={suoritusjako.secret}>
              <SuoritusjakoLink suoritusjako={suoritusjako} opiskeluoikeudet={opiskeluoikeudet} onRemove={onRemove}/>
            </li>
          ))}
        </ul>
      </div>
    )}
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

const NewSuoritusjako = ({opiskeluoikeudet, selectedSuoritusIds, onSuccess, showForm, canCancelForm, setRef}) => {
  const isPending = Atom(false)

  return (
    <div className='new-suoritusjako-wrapper' ref={setRef} tabIndex={-1}>
      {Bacon.combineWith(showForm, isPending, (form, pending) =>
        form ?
          pending ? <SuoritusjakoLinkPlaceholder transition='enter'/>
            : (
              <div className='suoritusjako-form__create-suoritusjako'>
                <div>
                  <div className='create-suoritusjako-header-row'>
                    <h2><Text name='Valitse jaettavat suoritustiedot'/></h2>
                    {canCancelForm.map(canCancel => canCancel && <ToggleButton toggleA={showForm} text='Peruuta' style='secondary'/>)}
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
          : <ToggleButton toggleA={showForm} text='Luo uusi' style='secondary'/>
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
    this.showLinkCreationSuccess = Atom(false)
    this.showLinkRemovalSuccess = Atom(false)
    this.canCancelForm = this.suoritusjaot.map(R.complement(R.isEmpty))
  }

  componentDidMount() {
    this.suoritusjaot.slidingWindow(2).onValue(([prev, now]) => {
      if (!prev) return

      if (!now) this.showNewSuoritusjakoForm.set(R.isEmpty(prev))
      else if (R.isEmpty(now)) this.showNewSuoritusjakoForm.set(true)
      else if (now.length > prev.length) this.showNewSuoritusjakoForm.set(false)
    })

    this.showNewSuoritusjakoForm.onValue(() => {
      this.showLinkCreationSuccess.set(false)
      this.showLinkRemovalSuccess.set(false)
    })

    Bacon.combineWith(this.showNewSuoritusjakoForm, this.suoritusjaot, (show, suoritusjaot) => show && !R.isEmpty(suoritusjaot))
      .map(v => v ? this.newSuoritusjakoFormElem : this.formSectionElem)
      .onValue(focusWithoutScrolling)

    this.props.showFormAtom.onValue(() => {
      this.showLinkCreationSuccess.set(false)
      this.showLinkRemovalSuccess.set(false)
    })

    Http.get(Url).onValue(jaot => this.suoritusjaot.set(jaot))
  }

  addLink(suoritusjako) {
    this.suoritusjaot.modify(list => R.append(suoritusjako, list))
    this.selectedSuoritusIds.set([])

    this.showLinkCreationSuccess.set(true)
    this.showLinkRemovalSuccess.set(false)
    focusWithoutScrolling(this.linkCreationSuccessElem)
  }

  removeLink(suoritusjako) {
    this.suoritusjaot.modify(list => R.without([suoritusjako], list))

    this.showLinkCreationSuccess.set(false)
    this.showLinkRemovalSuccess.set(true)
    focusWithoutScrolling(this.linkRemovalSuccessElem)
  }

  setNewSuoritusjakoRef(ref) {
    this.newSuoritusjakoFormElem = ref
  }

  render() {
    const {opiskeluoikeudet} = this.props

    return (
      <section className='suoritusjako-form textstyle-body' tabIndex={-1} ref={e => this.formSectionElem = e}>
        {this.showLinkRemovalSuccess.map(shouldShow => shouldShow && (
          <div tabIndex={-1} ref={e => this.linkRemovalSuccessElem = e} className='link-successful'>
            <div className='link-successful-icon'/>
            <Text name='Jakolinkin poistaminen onnistui.'/>
          </div>))}
        {this.showLinkCreationSuccess.map(shouldShow => shouldShow && (
          <div ref={e => this.linkCreationSuccessElem = e} tabIndex={-1} className='link-successful'>
            <div className='link-successful-icon'/>
            <Text name='Jakolinkin luominen onnistui.'/>
          </div>))}
        {this.suoritusjaot.map(suoritusjaot => R.isEmpty(suoritusjaot) && <Ingressi/>)}
        <SuoritusjakoList
          baret-lift
          opiskeluoikeudet={opiskeluoikeudet}
          suoritusjaot={this.suoritusjaot}
          onRemove={this.removeLink.bind(this)}
          showLinkCreationSuccess={this.showLinkCreationSuccess}
        />
        <NewSuoritusjako
          opiskeluoikeudet={opiskeluoikeudet}
          selectedSuoritusIds={this.selectedSuoritusIds}
          onSuccess={this.addLink.bind(this)}
          showForm={this.showNewSuoritusjakoForm}
          canCancelForm={this.canCancelForm}
          setRef={this.setNewSuoritusjakoRef.bind(this)}
        />
      </section>
    )
  }
}
