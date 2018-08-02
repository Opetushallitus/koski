import React from 'baret'
import Bacon from 'baconjs'
import Text from '../i18n/Text'
import ChevronUpIcon from '../icons/ChevronUpIcon'
import ChevronDownIcon from '../icons/ChevronDownIcon'
import Http from '../util/http'
import Spinner from '../omadata/Spinner'
import {formatFinnishDate, ISO2FinnishDate, parseISODate} from '../date/date'
import {userP} from '../util/user'
import {getBirthdayFromEditorRes} from '../util/util'
import ModalDialog from '../editor/ModalDialog'
import ErrorPage from '../omadata/ErrorPage'


const getBirthDate = editorResponse => {
  if (!editorResponse) return

  return formatFinnishDate(
    parseISODate(
      getBirthdayFromEditorRes(editorResponse)))
}

export class Kayttooikeudet extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loading: true,
      showDeleteConfirm: false,
      removeId: '',
      valtuudet: []
    }

    this.removePermission = this.removePermission.bind(this)
    this.showDeleteConfirm = this.showDeleteConfirm.bind(this)
    this.hideDeleteConfirm = this.hideDeleteConfirm.bind(this)
    this.onHttpError = this.onHttpError.bind(this)
  }

  componentDidMount() {
    const valtuutusS = Http.cachedGet('/koski/api/omadata/valtuutus', {errorHandler: () => this.onHttpError()})
    const birhtdayS = Http.cachedGet('/koski/api/omattiedot/editor', {errorHandler: () => this.onHttpError()}).map(getBirthDate)

    Bacon.combineAsArray(valtuutusS, birhtdayS)
      .onValue(([valtuudet, birthday]) => this.setState({valtuudet, birthday, loading: false}))
  }

  onHttpError() {
    this.setState({error: true, loading: false})
  }

  showDeleteConfirm(removeId) {
    this.setState({showDeleteConfirm: true, removeId})
  }

  hideDeleteConfirm() {
    this.setState({showDeleteConfirm: false, removeId: ''})
  }

  removePermission(removeAsiakasId) {
    Http.delete(`/koski/api/omadata/valtuutus/${removeAsiakasId}`, {errorHandler: () => this.onHttpError()})
      .onValue(() => {
        this.setState(
          prevState => ({
            valtuudet: prevState.valtuudet.filter(_=>_.asiakasId !== removeAsiakasId),
            showDeleteConfirm: false,
            removeId: ''
          })
        )
      })
  }

  render() {
    const {loading, valtuudet, birthday, showDeleteConfirm, removeId, error} = this.state
    const removeName = valtuudet.reduce((acc, i) => i.asiakasId === removeId ? i.asiakasName : acc, '')

    if (loading) return <Spinner />
    if (error) return <ErrorPage />

    return (
      <div className='omattiedot-kayttoluvat'>
        <Headline birthday={birthday}/>
        <Kayttoluvat kayttoluvat={valtuudet} removeCallback={this.showDeleteConfirm}/>
        {
          showDeleteConfirm &&
          <ModalDialog
            fullscreen={true}
            onDismiss={this.hideDeleteConfirm}
            onSubmit={() => this.removePermission(removeId)}
            okTextKey={'Kyllä, poista lupa'}
            cancelTextKey='Älä poista lupaa'
            children={
              <div className='kayttoluvat-modal-container'>
                <Text name={'Olet poistamassa palveluntarjoajalle'}/>
                <span>{` "${removeName}" `}</span>
                <Text name={'annettua lupaa nähdä opintoihisi liittyviä tietoja. ' +
                  'Poistaessasi luvan, voit menettää palveluntarjoajan opintoihisi liittyvät edut'}/>
              </div>}
          />}
      </div>
    )
  }
}

const Headline = ({birthday}) => (
  <div className='kayttoluvat-headline' tabIndex={0}>
    <div className='kayttoluvat-info'>
      <h1><Text name='Tietojeni käyttöluvat'/></h1>
      <div className='ebin'>
      <Text className='info' name={'Tällä sivulla voit tarkastella ja hallinnoida antamiasi käyttölupia tietoihisi. ' +
        'Lisäksi näet mitkä tahot, esim. viranomaiset, ovat katsoneet tietojasi.'}
      />
      </div>
    </div>
    <h3 className='oppija-nimi'><span className='nimi'>{userP.map(user => user && user.name)}</span><span className='pvm'>{` s. ${birthday}`}</span></h3>
  </div>
)

class Kayttoluvat extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      expanded: false
    }

    this.toggleExpand = this.toggleExpand.bind(this)
  }

  toggleExpand() {
    this.setState(prevState => ({expanded: !prevState.expanded}))
  }

  render() {
    const {expanded} = this.state
    const {kayttoluvat, removeCallback} = this.props
    const hasKayttolupia = kayttoluvat.length > 0

    return (
      <div className='kayttoluvat-container'>
        <div className='kayttoluvat-expander'>
          <button className='kayttolupa-button' onClick={() => this.toggleExpand()} aria-pressed={expanded}>
            <div className='button-container'>
              <div className='expander-text'><h2><Text name='Annetut käyttöluvat' /></h2></div>
              <div className='expand-icon'>
                {expanded
                  ? <ChevronUpIcon/>
                  : <ChevronDownIcon/>}
              </div>
            </div>
          </button>
        </div>
        <hr className='divider' />
        {
          expanded && (hasKayttolupia
            ? kayttoluvat.map((lupa, index) =>
              (<Kayttolupa
                key={index}
                kayttolupa={lupa}
                removeCallback={removeCallback}
              />))
            : <NoMyDataPermissions/>)
        }
      </div>
    )
  }
}

const Kayttolupa = ({kayttolupa, removeCallback}) => {
  const {asiakasName, asiakasId, expirationDate, timestamp} = kayttolupa
  const expDateInFinnish = formatFinnishDate(parseISODate(expirationDate))
  const timestampInFinnish = ISO2FinnishDate(timestamp)

  return (
    <div className='kayttolupa-container' tabIndex={0}>

      <h3 className='asiakas'>{asiakasName}</h3>

      <div className='bottom-items'>

        <div className='container'>

          <div className='voimassaolo'>
            <div className='teksti' ><Text name='Lupa voimassa'/></div>
            <div className='aikaleima'>
              <span className='mobile-whitespace'>{': '}</span><span> {`${timestampInFinnish} - ${expDateInFinnish}`}</span>
            </div>
          </div>

          <div className='oikeudet'>
            <span className='list-label'><Text name='Palveluntarjoaja näkee seuraavat opintoihisi liittyvät tiedot'/>{':'}</span>
            <ul>
              <li>
                <Text name='Oppilaitosten läsnäolotiedot' />
              </li>
            </ul>
          </div>

        </div>

        <div className='peru-lupa'>
          <button className='inline-link-button' onClick={() => removeCallback(asiakasId)}><Text name='Peru lupa'/></button>
        </div>

      </div>

    </div>
  )
}

const NoMyDataPermissions = () => (
  <div className='no-permission'>
    <Text name={'Et ole tällä hetkellä antanut millekään palveluntarjoajalle lupaa nähdä opintotietojasi Oma Opintopolusta. ' +
    'Luvan myöntäminen tapahtuu kyseisen palvelutarjoajan sivun kautta.'}/>
  </div>
)
