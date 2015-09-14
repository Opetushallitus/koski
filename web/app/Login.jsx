import React from "react"
import Bacon from "baconjs"

export const Login = (props) => (
    <form class="login">
        <input id="tunnus" placeholder="Tunnus"></input>
        <input id="salasana" placeholder="Salasana"></input>
        <input type="submit"></input>
    </form>
)

export const userP = Bacon.constant({oid: 1234, name: 'Kalle Kayttaja'})

