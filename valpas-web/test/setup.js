const failOnConsole = require("jest-fail-on-console")

failOnConsole()

globalThis.fetch = jest.fn(
  async () =>
    /** @type {Response} */ ({
      ok: true,
      status: 200,
      json: async () => ({
        huom: "fetch-kutsut on mockattu, kts. test/setup.js",
      }),
    }),
)

jest.setTimeout(5 * 60 * 1000)
