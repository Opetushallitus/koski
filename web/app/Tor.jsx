import React from "react"
import Bacon from "baconjs"
import http from "axios"

const events = new Bacon.Bus();

const OppijaHaku = React.createClass({
    render: () =>
        <div className="oppija-haku">
            <label>Opiskelija</label>
            <input onInput={(e) => events.push(e.target.value)}></input>
        </div>
});

events
    .throttle(200)
    .flatMapLatest(q => Bacon.fromPromise(http.get(`/oppija?nimi=${q}`)))
    .map(".data")
    .log();

React.render(
<OppijaHaku />,
    document.getElementById('content')
);

