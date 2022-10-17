import React from 'react'
import Text from '../../i18n/Text'
import { parseFinnishDate } from '../../date/date'

export class DateInputFeedback extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      isVisible: false
    }
  }

  componentDidMount() {
    const { feedbackBus } = this.props
    feedbackBus.onError(() => this.handleError())
    feedbackBus
      .filter((val) => val !== null && val !== undefined)
      .onValue((val) => this.handleValue(val))
  }

  handleError() {
    this.setState({ isVisible: true, renderFn: Error })
  }

  handleValue(value) {
    if (this.timeoutID) clearTimeout(this.timeoutID)

    if (value.expirationDate) {
      this.setState({ isVisible: true, renderFn: UpdatedSuccess })
      this.timeoutID = setTimeout(
        () => this.setState({ isVisible: false }),
        2000
      )
      return
    }

    const { futureValidator, yearValidator } = this.props
    const date = parseFinnishDate(value)

    if (!date) this.setState({ isVisible: true, renderFn: InvalidInput })
    else if (!yearValidator(date))
      this.setState({ isVisible: true, renderFn: MaxYear })
    else if (!futureValidator(date))
      this.setState({ isVisible: true, renderFn: InvalidInput })
    else this.setState({ isVisible: false })
  }

  render() {
    const { isVisible, renderFn } = this.state
    return (
      <div className="date-input-feedback" aria-live="polite">
        {isVisible && <div className="feedback">{renderFn()}</div>}
      </div>
    )
  }
}

const InvalidInput = () => [
  <div key="icon" className="invalid-date-input" />,
  <Text key="text" name="Virheellinen päivämäärä" />
]
const MaxYear = () => [
  <div key="icon" className="invalid-date-input" />,
  <Text key="text" name="Pisin voimassaoloaika on vuosi" />
]
const Error = () => [
  <div key="icon" className="invalid-date-input" />,
  <Text key="text" name="Päivitys epäonnistui" />
]
const UpdatedSuccess = () => [
  <div key="icon" className="save-confirmation" />,
  <Text key="text" name="Muutokset tallennettu" />
]
