import React from "react"
import Bacon from "baconjs"

const events = new Bacon.Bus();

const OppijaHaku = React.createClass({
    render: () =>
        <div className="oppija-haku">
            <label>Opiskelija</label>
            <input onInput={(e) => events.push(e)}></input>
        </div>
});

events.log();

React.render(
<OppijaHaku />,
    document.getElementById('content')
);

