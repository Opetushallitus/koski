import React from 'baret'
import Bacon from 'baconjs'
import Http from '../../util/http'
import {formatFinnishDate, parseISODate} from '../../date/date'
import {getBirthdayFromEditorRes} from '../../util/util'
import Spinner from '../Spinner'
import {Error as ErrorDisplay} from '../../util/Error'
import ModalDialog from '../../editor/ModalDialog'
import {LuvanHallintaHeadline} from './LuvanHallintaHeadline'
import {Kayttoluvat} from './KayttoLuvat'
import Text from '../../i18n/Text'
import {t} from '../../i18n/i18n'

const getBirthDate = editorResponse => {
  if (!editorResponse) return

  return formatFinnishDate(
    parseISODate(
      getBirthdayFromEditorRes(editorResponse)))
}

export class LuvanHallinta extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loading: true,
      showDeleteConfirm: false,
      error: undefined,
      removeId: '',
      valtuudet: [],
      birthday: undefined
    }

    this.removePermission = this.removePermission.bind(this)
    this.showDeleteConfirm = this.showDeleteConfirm.bind(this)
    this.hideDeleteConfirm = this.hideDeleteConfirm.bind(this)
    this.onHttpGetError = this.onHttpGetError.bind(this)
  }

  componentDidMount() {
    const valtuutusS = Http.cachedGet('/koski/api/omadata/valtuutus', {errorHandler: () => this.onHttpGetError()})
    const birhtdayS = Http.cachedGet('/koski/api/omattiedot/editor', {errorHandler: () => this.onHttpGetError()}).map(getBirthDate)

    Bacon.combineAsArray(valtuutusS, birhtdayS)
      .onValue(([valtuudet, birthday]) => this.setState({valtuudet, birthday, loading: false}))
  }

  onHttpGetError(error = t('Järjestelmässä tapahtui virhe')) {
    this.setState({error, loading: false})
  }

  showDeleteConfirm(removeId) {
    this.setState({showDeleteConfirm: true, removeId, error: undefined})
  }

  hideDeleteConfirm() {
    this.setState({showDeleteConfirm: false, removeId: ''})
  }

  removePermission(removeAsiakasId) {
    Http.delete(`/koski/api/omadata/valtuutus/${removeAsiakasId}`, {
      errorHandler: () => {
        this.setState({
          showDeleteConfirm: false,
          error: t('Luvan poistaminen epäonnistui')
        })
      }
    })
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
    const renderError = error ? <ErrorDisplay error={{text: error}} /> :null

    if (loading) return <Spinner />

    return (
      <div>
        {renderError}
        <div className='omattiedot-kayttoluvat'>
          <LuvanHallintaHeadline birthday={birthday}/>
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
                  <Text name='Olet poistamassa palveluntarjoajalle'/>
                  <span>{` "${removeName}" `}</span>
                  <Text name={'annettua lupaa nähdä opintoihisi liittyviä tietoja. ' +
                  'Poistaessasi luvan, voit menettää palveluntarjoajan opintoihisi liittyvät edut'}/>
                </div>}
            />}
        </div>
      </div>
    )
  }
}
