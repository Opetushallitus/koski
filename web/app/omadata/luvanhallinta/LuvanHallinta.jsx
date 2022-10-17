import React from 'baret'
import Bacon from 'baconjs'
import Http from '../../util/http'
import { formatFinnishDate, parseISODate } from '../../date/date'
import { getBirthdayFromEditorRes } from '../../util/util'
import Spinner from '../Spinner'
import { Error as ErrorDisplay } from '../../util/Error'
import ModalDialog from '../../editor/ModalDialog'
import { LuvanHallintaHeadline } from './LuvanHallintaHeadline'
import { LuvanHallintaDisclaimer } from './LuvanHallintaDisclaimer'
import NavBar from './NavBar'
import { Kayttoluvat } from './KayttoLuvat'
import Text from '../../i18n/Text'
import { t } from '../../i18n/i18n'

const getBirthDate = (editorResponse) => {
  if (!editorResponse) return

  return formatFinnishDate(
    parseISODate(getBirthdayFromEditorRes(editorResponse))
  )
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
    const valtuutusS = Http.cachedGet('/koski/api/omadata/valtuutus', {
      errorHandler: () => this.onHttpGetError()
    })
    const birthDayS = Http.cachedGet('/koski/api/omattiedot/editor', {
      errorHandler: (e) => {
        if (e.httpStatus !== 404) {
          this.onHttpGetError()
        } else {
          this.setState({ error: undefined, loading: false })
        }
      }
    }).map(getBirthDate)

    Bacon.combineAsArray(valtuutusS, birthDayS).onValue(
      ([valtuudet, birthday]) =>
        this.setState({ valtuudet, birthday, loading: false })
    )
  }

  onHttpGetError(error = t('Järjestelmässä tapahtui virhe')) {
    this.setState({ error, loading: false })
  }

  showDeleteConfirm(removeId) {
    this.setState({ showDeleteConfirm: true, removeId, error: undefined })
  }

  hideDeleteConfirm() {
    this.setState({ showDeleteConfirm: false, removeId: '' })
  }

  removePermission(removeAsiakasId) {
    Http.delete(`/koski/api/omadata/valtuutus/${removeAsiakasId}`, {
      errorHandler: () => {
        this.setState({
          showDeleteConfirm: false,
          error: t('Luvan poistaminen epäonnistui')
        })
      }
    }).onValue(() => {
      this.setState((prevState) => ({
        valtuudet: prevState.valtuudet.filter(
          (_) => _.asiakasId !== removeAsiakasId
        ),
        showDeleteConfirm: false,
        removeId: ''
      }))
    })
  }

  render() {
    const { loading, valtuudet, birthday, showDeleteConfirm, removeId, error } =
      this.state
    const removeName = valtuudet.reduce(
      (acc, i) => (i.asiakasId === removeId ? i.asiakasName : acc),
      ''
    )
    const renderError = error ? <ErrorDisplay error={{ text: error }} /> : null

    if (loading)
      return (
        <div className="omattiedot-kayttoluvat">
          <Spinner />
        </div>
      )

    return (
      <div>
        {renderError}
        <div className="omattiedot-kayttoluvat">
          <LuvanHallintaHeadline birthday={birthday} />
          <NavBar />
          <Kayttoluvat
            kayttoluvat={valtuudet}
            removeCallback={this.showDeleteConfirm}
          />
          {showDeleteConfirm && (
            <ModalDialog
              fullscreen={true}
              onDismiss={this.hideDeleteConfirm}
              onSubmit={() => this.removePermission(removeId)}
              okTextKey={'Kyllä, poista lupa'}
              cancelTextKey="Älä poista lupaa"
            >
              <div className="kayttoluvat-modal-container">
                <Text name="Olet poistamassa palveluntarjoajalle" />
                <span>{` "${removeName}" `}</span>
                <Text
                  name={
                    'annettua lupaa nähdä opintoihisi liittyviä tietoja. ' +
                    'Poistaessasi luvan, voit menettää palveluntarjoajan opintoihisi liittyvät edut'
                  }
                />
              </div>
            </ModalDialog>
          )}
          <LuvanHallintaDisclaimer />
        </div>
      </div>
    )
  }
}
