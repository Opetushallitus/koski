import React from 'baret'
import Bacon from 'baconjs'
import {CopyableText} from '../../components/CopyableText'
import Text from '../../i18n/Text'
import DateInput from '../../date/DateInput'
import {formatISODate, ISO2FinnishDateTime, parseISODate} from '../../date/date'
import Http from '../../util/http'
import ModalDialog from '../../editor/ModalDialog'
import {DateInputFeedback} from './DateInputFeedback'
import ModalMount from '../../components/ModalMount'

const ApiBaseUrl = '/koski/api/suoritusjakoV2'

const doDelete = secret => Http.post(`${ApiBaseUrl}/delete`, {secret})
const doUpdate = (secret, expirationDate) => Http.post(`${ApiBaseUrl}/update`, {secret, expirationDate})

export const SuoritusjakoLinkPlaceholder = ({transition}) =>
  <div className={`suoritusjako-link--placeholder${transition === 'enter' ? ' transition-enter' : ''}`}/>

export class SuoritusjakoLink extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      isDeletePending: false,
      isDateUpdatePending: false,
      showDeleteConfirmation: false
    }

    this.dateChangeBus = new Bacon.Bus()
    this.feedbackBus = new Bacon.Bus()
  }

  componentDidMount() {
    const debounced = this.dateChangeBus
      .toProperty()
      .changes()
      .debounce(500)

    debounced.onValue(() => this.setState({isDateUpdatePending: true}))

    this.updateBus = debounced
      .flatMapLatest(date => date && doUpdate(this.props.suoritusjako.secret, formatISODate(date)))

    this.updateBus.onValue(val => {
      this.setState({isDateUpdatePending: false})
      this.feedbackBus.push(val)
    })
  }

  static isDateInFuture(date) {
    const today = new Date()
    const tomorrow = new Date()
    tomorrow.setDate(today.getDate() + 1)
    tomorrow.setHours(0,0,0,0)

    return date.getTime() >= tomorrow.getTime()
  }

  static isDateWithinYear(date) {
    const today = new Date()
    const limit = new Date(today.getFullYear() + 1, today.getMonth(), today.getDate() + 1)
    limit.setHours(0,0,0,0)

    return date.getTime() < limit.getTime()
  }

  deleteSelf() {
    const {isDateUpdatePending} = this.state
    const {suoritusjako, onRemove} = this.props
    const {secret} = suoritusjako

    if (isDateUpdatePending) return

    this.setState({isDeletePending: true},
      () => {
        const res = doDelete(secret)
        res.onValue(() => onRemove(suoritusjako))
        res.onError(() => this.setState({isDeletePending: false}))
      }
    )
  }

  confirmDelete() {
    this.setState({showDeleteConfirmation: true})
  }

  cancelConfimDelete() {
    this.setState({showDeleteConfirmation: false})
  }

  render() {
    const {isDeletePending, isDateUpdatePending, showDeleteConfirmation} = this.state
    const {suoritusjako} = this.props
    const {secret, expirationDate, timestamp} = suoritusjako
    const url = `${window.location.origin}/koski/opinnot/${secret}`
    const labelId = `date-input-${timestamp}`

    return isDeletePending ? <SuoritusjakoLinkPlaceholder/>
      : (
        <div className='suoritusjako-link'>
          <div className='suoritusjako-link__top-container'>
            <div className="suoritusjako-link__link-container">
              <span className="suoritusjako-link__timestamp"><Text name="Jakolinkki luotu"/> {ISO2FinnishDateTime(timestamp).replace(' ', ' klo ')}</span>
              <CopyableText
                className='suoritusjako-link__url'
                message={url}
                multiline={false}
                buttonText='Kopioi linkki'
              />
            </div>
            <div className='suoritusjako-link__expiration'>
              <label htmlFor={labelId}><Text name='Linkin voimassaoloaika päättyy'/></label>
              <div style={{display: 'inline'}} className={isDateUpdatePending ? 'ajax-indicator-right' : ''}>
              <DateInput
                value={parseISODate(expirationDate)}
                valueCallback={date => this.dateChangeBus.push(date)}
                validityCallback={(isValid, stringInput)=> {
                  !isValid && this.dateChangeBus.push(null)
                  this.feedbackBus.push(stringInput)
                }}
                isAllowedDate={d => SuoritusjakoLink.isDateInFuture(d) && SuoritusjakoLink.isDateWithinYear(d)}
                isPending={isDateUpdatePending}
                inputId={labelId}
              />
              <DateInputFeedback
                feedbackBus={this.feedbackBus}
                futureValidator={SuoritusjakoLink.isDateInFuture}
                yearValidator={SuoritusjakoLink.isDateWithinYear}
              />
             </div>
            </div>
          </div>
          <div className='suoritusjako-link__bottom-container'>
            <div className='suoritusjako-link__preview'>
              <a className='text-button-small' target='_blank' href={url}><Text name='Katso, miltä suoritusote näyttää selaimessa'/></a>
            </div>
            <div className='suoritusjako-link__remove'>
              <button className={`text-button-small${(isDateUpdatePending ? '--disabled' : '')}`} onClick={this.confirmDelete.bind(this)} disabled={isDateUpdatePending}>
                <Text name='Poista linkki käytöstä'/>
              </button>

              {showDeleteConfirmation && (
                <ModalMount>
                  <ModalDialog
                    fullscreen={true}
                    onDismiss={this.cancelConfimDelete.bind(this)}
                    onSubmit={this.deleteSelf.bind(this)}
                    submitOnEnterKey={false}
                    okTextKey='Kyllä, poista linkki käytöstä'
                    cancelTextKey='Älä poista linkkiä'
                  >
                    <Text name='Linkin poistamisen jälkeen kukaan ei pääse katsomaan opintosuorituksiasi, vaikka olisit jakanut tämän linkin heille.'/>
                  </ModalDialog>
                </ModalMount>
              )}
            </div>
          </div>
        </div>
      )
  }
}
