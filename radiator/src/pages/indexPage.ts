import * as string from "fp-ts/String";
import { pipe } from "fp-ts/lib/function";
import * as R from "fp-ts/Record";
import { ReprHost, ReprService, ReprState } from "../representationalState";

function htmlEntities(str: string) {
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&apos");
}

export const indexPage = (state: ReprState) => `
<!DOCTYPE html>
<html>
    <head>
        <title>KOSKI ${htmlEntities(state.env).toUpperCase()}</title>
        <meta charset="UTF-8" />
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Nova+Square&display=swap" rel="stylesheet"> 
        <style>
            body {
                background: #000;
                padding: 10px;
                margin: 0;
                font-family: 'Nova Square', sans-serif;
            }
            .host .flex {
                display: flex;
                align-items: flex-start;
                justify-content: space-evenly;
                flex-flow: row wrap;
            }
            .host {
                padding: 20px;
                border: 5px solid black;
                border-radius: 8px;
                color: white;
                background: #111 !important;
                margin-bottom: 20px;
            }
            .service {
                background: #222;
                width: 300px;
                min-height: 60px;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: stretch;
                color: black;
                padding: 10px 30px;
                margin-bottom: 20px;
                text-align: center;
                border: 2px solid;
                border-radius: 8px;
                box-shadow: rgba(0, 0, 0, 0.05) 0px 6px 24px 0px, rgba(0, 0, 0, 0.08) 0px 0px 0px 1px;
            }
            h2 {
                text-align: center;
                margin: 10px;
                margin-bottom: 40px;
            }
            h3 {
                font-weight: 400;
            }
            .service .service {
                font-size: 80%;
            }
            .ok {
                border-color: #C2FF48;
                background: #222;
                color: #C2FF48;
            }
            .degraded {
                border-color: #FFB648;
                background: #FFB648;
            }
            .down {
                border-color: #FF4848;
                background: #992424;
                color: #ffffff;
            }
            .lost {
                border-color: #888;
                background: #888;
            }
        </style>
        <meta http-equiv="refresh" content="60; URL=/">
    </head>
    <body>
        ${renderHosts(state.env, state.hosts)}
    </body>
</html>
`;

const renderHosts = (env: string, hosts: Record<string, ReprHost>): string =>
  pipe(
    hosts,
    R.mapWithIndex(
      (hostName: string, host: ReprHost) => `
        <div class="host ${htmlEntities(host.status)}">
            ${
              hostName
                ? `<h2>${htmlEntities(hostName)} (${htmlEntities(env)})</h2>`
                : ""
            }
            <div class="flex">
                ${host.services.map(renderService).join("\n")}
            </div>
        </div>
    `
    ),
    R.collect(string.Ord)((_, a) => a),
    (as) => as.join("\n")
  );

const renderService = (instance: ReprService) => `
    <div class="service ${htmlEntities(instance.status)}">
        <h3>${instance.status === "ok" ? "✓" : "⚠"} ${htmlEntities(
  instance.name
)}</h3>
        ${instance.message ? `<p>${htmlEntities(instance.message)}</p>` : ""}
    </div>
`;
