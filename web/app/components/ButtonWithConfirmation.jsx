import React from 'react'
import Text from '../i18n/Text'

class ButtonWithConfirmation extends React.Component {
  constructor(props) {
    super(props)
    this.state = { isActionRequested: false }
  }

  render() {
    const { text, confirmationText, cancelText, action, className, confirmationClassName } = this.props

    const isActionRequested = this.state.isActionRequested

    return isActionRequested
      ? (
        <div className={className}>
          <a onClick={() => this.setState({ isActionRequested: false })}>
            <Text name={cancelText}/>
          </a>

          <button className={`koski-button ${(confirmationClassName ? confirmationClassName : '')}`} onClick={action}>
            <Text name={confirmationText}/>
          </button>
        </div>
      )
      : (
        <a className={className} onClick={() => this.setState({ isActionRequested: true })}>
          <Text name={text} />
        </a>
      )
  }
}

export default ButtonWithConfirmation
