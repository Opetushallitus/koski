import React from 'react'
import Bacon from 'baconjs'
import {CopyableText} from '../../components/CopyableText'
import Text from '../../i18n/Text'
import DateInput from '../../date/DateInput'
import {formatISODate, parseISODate} from '../../date/date'
import Http from '../../util/http'

const ApiBaseUrl = '/koski/api/suoritusjako'

const doDelete = secret => Http.post(`${ApiBaseUrl}/delete`, {secret})
const doUpdate = (secret, expirationDate) => Http.post(`${ApiBaseUrl}/update`, {secret, expirationDate})

export const SuoritusjakoLinkPlaceholder = () => <div className='suoritusjako-link--placeholder'/>

export class SuoritusjakoLink extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      isDeletePending: false
    }

    this.dateValue = new Bacon.Bus()
  }

  componentDidMount() {
    this.dateValue
      .toProperty()
      .changes()
      .debounce(1000)
      .onValue(date => doUpdate(this.props.suoritusjako.secret, formatISODate(date)))
  }

  render() {
    const {suoritusjako, onRemove} = this.props
    const {secret, expirationDate} = suoritusjako
    const url = `${window.location.origin}/koski/opinnot/${secret}`

    return this.state.isDeletePending ? <SuoritusjakoLinkPlaceholder/>
      : (
        <div className='suoritusjako-link'>
          <div className='suoritusjako-link__top-container'>
            <CopyableText className='suoritusjako-link__url' message={url} multiline={false}/>
            <div className='suoritusjako-link__expiration'>
              <label>
                <Text name='Linkin voimassaoloaika'/>
                <Text name='Päättyy'/>
                <DateInput
                  value={parseISODate(expirationDate)}
                  valueCallback={date => this.dateValue.push(date)}
                />
              </label>
            </div>
          </div>
          <div className='suoritusjako-link__bottom-container'>
            <div className='suoritusjako-link__preview'>
              <a target='_blank' href={url}><Text name='Esikatsele'/></a>
            </div>
            <div className='suoritusjako-link__remove'>
              <a onClick={() => this.setState({isDeletePending: true}, () => {
                  const res = doDelete(secret)
                  res.onValue(() => onRemove(suoritusjako))
                  res.onError(() => this.setState({isDeletePending: false}))
                }
              )}>
                <Text name='Poista linkki käytöstä'/>
              </a>
            </div>
          </div>
        </div>
      )
  }
}
