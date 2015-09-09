import React from "react"

var OppijaHaku = React.createClass({
    render: () =>
        <div className="oppija-haku">
            <label>Opiskelija</label>
            <input></input>
        </div>
});

React.render(
<OppijaHaku />,
    document.getElementById('content')
);

