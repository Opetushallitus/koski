import React from 'react'
import Text from "../../i18n/Text";


export class DateInputFeedback extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      isVisible: false
    }
  }

  componentDidMount() {
    const {updateBus} = this.props
    updateBus.onError(() => this.handleError())
    updateBus.onValue(val => this.handleUpdate(val))
  }

  handleError() {
    this.setState({isVisible: true, renderFn: this.renderError})
  }

  handleUpdate(value) {
    if (this.timeoutID) {
      clearTimeout(this.timeoutID)
    }

    if (!value) {
      this.setState({isVisible: true, renderFn: this.renderInvalid})

    } else if (value.expirationDate) {
      this.setState({isVisible: true, renderFn: this.renderUpdated})
      this.timeoutID = setTimeout(() => this.setState({isVisible: false}), 2000)
    }
  }

  renderInvalid() {
    return (
      <div>
        <div className="invalid-date-input"/>
        <Text name="Virheellinen päivämäärä"/>
      </div>
    )
  }

  renderUpdated() {
    return (
      <div>
        <div className="save-confirmation"/>
        <Text name="Muutokset tallennettu"/>
      </div>
    )
  }

  renderError() {
    return (
      <div>
        <div className="invalid-date-input"/>
        <Text name="Päivitys epäonnistui"/>
      </div>
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
