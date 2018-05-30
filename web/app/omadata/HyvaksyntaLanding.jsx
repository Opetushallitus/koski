import React from 'react';
import ReactDOM from 'react-dom';
import Hyvaksynta from './Hyvaksynta'
import Http from '../util/http';

class HyvaksyntaLanding extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      authorizationGiven: true,
      memberCode: 'hsl'
    }

    this.postAuthorization = this.postAuthorization.bind(this)
  }


  postAuthorization() {
    Http.post(`/koski/api/omadata/valtuutus/${this.state.memberCode}`, {})
      .doError((e) => {
        if (e && e.httpStatus === 401) {
          console.log(`Must be logged in before we can authorize ${this.state.memberCode}`)
        }
        console.log(`Failed to add permissions for ${this.state.memberCode}`)
        console.log(e)
      })
      .onValue((response) => {
        console.log(`Permissions added for ${this.state.memberCode}`)
        console.log(response)
      })
  }

  render() {
    return this.state.authorizationGiven ?
      <Hyvaksynta
        firstName={'Clara'}
        lastName={'Nieminen'}
        dateOfBirth={new Date('December 17, 1995 03:24:00')}
        onAcceptClick={this.postAuthorization}
      /> :
      <div>Hello world</div>
  }
}

ReactDOM.render((
  <div>
    <HyvaksyntaLanding/>
  </div>
), document.getElementById('content'))
