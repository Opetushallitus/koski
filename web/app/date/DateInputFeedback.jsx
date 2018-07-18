import React from 'react'
import Text from "../i18n/Text";


export class DateInputFeedback extends React.Component {
  constructor(props) {
    super(props)

    this.state ={
      isVisible: false
    }
  }

  componentDidMount() {
    const {dateChangeBus, updateBus} = this.props

    updateBus.onValue(val => this.showFeedback(val))

    dateChangeBus.onValue(val => this.showFeedback(val))
  }

  showFeedback(value) {
    if (this.timeoutID){
      clearTimeout(this.timeoutID)
    }

    const isValid = value !== null

    this.setState({isVisible: true, isValid})

    if (isValid) {
      this.timeoutID = setTimeout(() => this.setState({isVisible: false}), 2000)
    }
  }

  renderFeedbackImage(isValid) {
    return isValid
      ? <div className="save-confirmation" />
      : <div className="invalid-date-input" />
  }

  renderFeedbackText(isValid) {
    return isValid
      ? <Text name="Muutokset tallennettu" >''</Text>
      : <Text name="Virheellinen päivämäärä">''</Text>
  }

  renderFeedback(isValid) {
    return (
      <div>
        {this.renderFeedbackImage(isValid)}
        {this.renderFeedbackText(isValid)}
      </div>
    )
  }

  render() {
    const {isVisible, isValid} = this.state

    return (
      <div className="date-input-feedback">
        {isVisible && this.renderFeedback(isValid)}
      </div>
    )
  }
}
