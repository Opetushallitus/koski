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
import {
  Kayttolupa,
  NoMyDataPermissions,
  OAuth2Käyttölupa
} from './KayttoLuvat'
import Text from '../../i18n/Text'
import { t } from '../../i18n/i18n'
import { KoodistoProvider } from '../../appstate/koodisto'

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
      error: undefined,
      lupaToRemove: null,
      käyttöluvat: [],
      oauth2käyttöluvat: [],
      birthday: undefined
    }

    this.removePermission = this.removePermission.bind(this)
    this.showDeleteConfirm = this.showDeleteConfirm.bind(this)
    this.hideDeleteConfirm = this.hideDeleteConfirm.bind(this)
    this.onHttpGetError = this.onHttpGetError.bind(this)
  }

  componentDidMount() {
    const käyttöluvatS = Http.cachedGet('/koski/api/omadata/valtuutus', {
      errorHandler: () => this.onHttpGetError()
    })
    const oauth2käyttöluvatS = Http.cachedGet(
      '/koski/api/omadata-oauth2/resource-owner/active-consents',
      {
        errorHandler: (e) => {
          if (e.httpStatus === 404) {
            // Ignoorataan virhe kun api ei ole vielä päällä tuotannossa
            this.setState({ loading: false })
          } else {
            this.onHttpGetError()
          }
        }
      }
    )
    const birthDayS = Http.cachedGet('/koski/api/omattiedot/editor', {
      errorHandler: (e) => {
        if (e.httpStatus !== 404) {
          this.onHttpGetError()
        } else {
          this.setState({ error: undefined, loading: false })
        }
      }
    }).map(getBirthDate)

    Bacon.combineAsArray(käyttöluvatS, oauth2käyttöluvatS, birthDayS).onValue(
      ([käyttöluvat, oauth2käyttöluvat, birthday]) =>
        this.setState({
          käyttöluvat,
          oauth2käyttöluvat,
          birthday,
          loading: false
        })
    )
  }

  onHttpGetError(error = t('Järjestelmässä tapahtui virhe')) {
    this.setState({ error, loading: false })
  }

  showDeleteConfirm(lupa) {
    this.setState({ lupaToRemove: lupa })
  }

  hideDeleteConfirm() {
    this.setState({ lupaToRemove: null })
  }

  removePermission(lupa) {
    const url = lupa.oauth2
      ? `/koski/api/omadata-oauth2/resource-owner/active-consents/${lupa.id}`
      : `/koski/api/omadata/valtuutus/${lupa.id}`

    Http.delete(url, {
      errorHandler: () => {
        this.setState({
          showDeleteConfirm: false,
          error: t('Luvan poistaminen epäonnistui')
        })
      }
    }).onValue(() => {
      this.setState((prevState) => ({
        käyttöluvat: lupa.oauth2
          ? prevState.käyttöluvat
          : prevState.käyttöluvat.filter((_) => _.asiakasId !== lupa.id),
        oauth2käyttöluvat: lupa.oauth2
          ? prevState.oauth2käyttöluvat.filter((v) => v.codeSHA256 !== lupa.id)
          : prevState.oauth2käyttöluvat,
        lupaToRemove: null
      }))
    })
  }

  render() {
    const {
      loading,
      käyttöluvat,
      oauth2käyttöluvat,
      birthday,
      lupaToRemove,
      error
    } = this.state

    const hasKayttolupia = käyttöluvat.length + oauth2käyttöluvat.length > 0

    const renderError = error ? <ErrorDisplay error={{ text: error }} /> : null

    if (loading)
      return (
        <div className="omattiedot-kayttoluvat">
          <Spinner />
        </div>
      )

    return (
      <KoodistoProvider>
        <div>
          {renderError}
          <div className="omattiedot-kayttoluvat">
            <LuvanHallintaHeadline birthday={birthday} />
            <NavBar />
            <div className="kayttoluvat-container">
              <ul className="kayttolupa-list">
                {hasKayttolupia ? (
                  <>
                    {käyttöluvat.map((lupa) => (
                      <Kayttolupa
                        key={lupa.asiakasId}
                        kayttolupa={lupa}
                        removeCallback={this.showDeleteConfirm}
                      />
                    ))}
                    {oauth2käyttöluvat.map((lupa) => (
                      <OAuth2Käyttölupa
                        key={lupa.clientId}
                        kayttolupa={lupa}
                        removeCallback={this.showDeleteConfirm}
                      />
                    ))}
                  </>
                ) : (
                  <NoMyDataPermissions />
                )}
              </ul>
            </div>
            {lupaToRemove && (
              <ModalDialog
                fullscreen={true}
                onDismiss={this.hideDeleteConfirm}
                onSubmit={() => this.removePermission(lupaToRemove)}
                okTextKey={'Kyllä, poista lupa'}
                cancelTextKey="Älä poista lupaa"
              >
                <div className="kayttoluvat-modal-container">
                  <Text name="Olet poistamassa palveluntarjoajalle" />
                  <span>{` "${lupaToRemove.name}" `}</span>
                  <Text
                    name={'annettua lupaa nähdä opintoihisi liittyviä tietoja'}
                  />
                  {
                    // Näytetään vain HSL luville
                    lupaToRemove.oauth2 ? null : (
                      <>
                        <span> </span>
                        <Text
                          name={
                            'Poistaessasi luvan menetät palveluntarjoajan tarjoamat opintoihisi liittyvät edut'
                          }
                        />
                      </>
                    )
                  }
                </div>
              </ModalDialog>
            )}
            <LuvanHallintaDisclaimer />
          </div>
        </div>
      </KoodistoProvider>
    )
  }
}
