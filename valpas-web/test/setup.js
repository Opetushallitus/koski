const failOnConsole = require("jest-fail-on-console")
const fetchMock = require("jest-fetch-mock")

failOnConsole()

fetchMock.enableMocks()
fetchMock.mockResponse(
  '{"huom": "fetch-kutsut on mockailtu, kts. https://www.npmjs.com/package/jest-fetch-mock"}',
)

jest.setTimeout(5 * 60 * 1000)
