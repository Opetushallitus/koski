import React from 'react'
import Text from '../../i18n/Text'
import {parseFinnishDate} from '../../date/date'


export class DateInputFeedback extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      isVisible: false
    }
  }

  componentDidMount() {
    const {feedbackBus} = this.props
    feedbackBus.onError(() => this.handleError())
    feedbackBus
      .filter(val => val !== null && val !== undefined)
      .onValue(val => this.handleValue(val))
  }

  handleError() {
    this.setState({isVisible: true, renderFn: this.renderError})
  }

  handleValue(value) {
    if (this.timeoutID) clearTimeout(this.timeoutID)

    if (value.expirationDate) {
      this.setState({isVisible: true, renderFn: this.renderUpdated})
      this.timeoutID = setTimeout(() => this.setState({isVisible: false}), 2000)
      return
    }

    const {futureValidator, yearValidator} = this.props
    const date = parseFinnishDate(value)

    if (!date) this.setState({isVisible: true, renderFn: this.renderInvalid})
    else if (!yearValidator(date)) this.setState({isVisible: true, renderFn: this.renderMaxYear})
    else if (!futureValidator(date)) this.setState({isVisible: true, renderFn: this.renderInvalid})

  }

  renderMaxYear() {
    return (
      <div><div className="invalid-date-input"/><Text name="Maksimi voimassaoloaika on vuosi"/></div>
    )
  }

  renderInvalid() {
    return (
      <div><div className="invalid-date-input"/><Text name="Virheellinen päivämäärä"/></div>
    )
  }

  renderUpdated() {
    return (
      <div><div className="save-confirmation"/><Text name="Muutokset tallennettu"/></div>
    )
  }

  renderError() {
    return (
      <div><div className="invalid-date-input"/><Text name="Päivitys epäonnistui"/></div>
    )
  }

  render() {
    const {isVisible, renderFn} = this.state

    return (
      <div className="date-input-feedback">
        {isVisible && renderFn()}
      </div>
    )
  }
}
