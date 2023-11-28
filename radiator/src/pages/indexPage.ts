import * as string from 'fp-ts/String'
import {pipe} from 'fp-ts/lib/function'
import * as R from 'fp-ts/Record'
import {ReprHost, ReprService, ReprState} from '../representationalState'

function htmlEntities(str: string) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos')
}

export const indexPage = (state: ReprState) => `
<!DOCTYPE html>
<html>
    <head>
        <title>KOSKI ${htmlEntities(state.env).toUpperCase()}</title>
        <meta charset="UTF-8" />
        <style>
            body {
                background: #333;
                padding: 10px;
                margin: 0;
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
                background: none !important;
                margin-bottom: 20px;
            }
            .service {
                width: calc((100% - 160px) / 4);
                min-height: 60px;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: stretch;
                color: black;
                padding: 10px;
                margin-bottom: 10px;
                text-align: center;
                border-radius: 8px;
                box-shadow: rgba(0, 0, 0, 0.05) 0px 6px 24px 0px, rgba(0, 0, 0, 0.08) 0px 0px 0px 1px;
            }
            h2 {
                margin: 10px;
            }
            .service .service {
                font-size: 80%;
            }
            .ok {
                border-color: #C2FF48;
                background: #C2FF48;
            }
            .degraded {
                border-color: #FFB648;
                background: #FFB648;
            }
            .down {
                border-color: #FF4848;
                background: #FF4848;
            }
            .lost {
                border-color: #888;
                background: #888;
            }
        </style>
        <meta http-equiv="refresh" content="60; URL=/">
    </head>
    <body>
        <div style="padding: 16px; font-size: 1.5rem; color: white; text-transform: uppercase; font-weight: bold; background-color: gray; margin-bottom: 16px;">${htmlEntities(
          state.env
        )}</div>
        ${renderHosts(state.hosts)}
    </body>
</html>
`

const renderHosts = (hosts: Record<string, ReprHost>): string =>
  pipe(
    hosts,
    R.mapWithIndex(
      (hostName: string, host: ReprHost) => `
        <div class="host ${htmlEntities(host.status)}">
            <h2>${htmlEntities(hostName)}</h2>
            <div class="flex">
                ${host.services.map(renderService).join('\n')}
            </div>
        </div>
    `
    ),
    R.collect(string.Ord)((_, a) => a),
    as => as.join('\n')
  )

const renderService = (instance: ReprService) => `
    <div class="service ${htmlEntities(instance.status)}">
        <h3>${htmlEntities(instance.name)}</h3>
        ${instance.message ? `<p>${htmlEntities(instance.message)}</p>` : ''}
    </div>
`
