import React from 'react';
import ReactDOM from 'react-dom';
import Hyvaksynta from './Hyvaksynta'

class HyvaksyntaLanding extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      authorizationGiven: true
    }
  }

  render() {
    return this.state.authorizationGiven ?
      <Hyvaksynta firstName={'Clara'} lastName={'Nieminen'} dateOfBirth={new Date('December 17, 1995 03:24:00')}/> :
      <div>Hello world</div>
  }
}

ReactDOM.render((
  <div>
    <HyvaksyntaLanding/>
  </div>
), document.getElementById('content'))
