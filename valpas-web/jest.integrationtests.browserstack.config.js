const baseConfig = require("./jest.integrationtests.config")

const parseSoftware = (nameKey, versionKey, defaultName) => (env) => {
  const [name, version] = env.split(" ").map((t) => t.toLowerCase())
  return { [nameKey]: name || defaultName, [versionKey]: version }
}

const parseOS = parseSoftware("os", "osVersion")
const parseBrowser = parseSoftware("browserName", "browserVersion")

const browser = parseBrowser(process.env.BROWSERSTACK_BROWSER || "chrome")
const os = parseOS(process.env.BROWSERSTACK_OS || "windows 8")

module.exports = {
  ...baseConfig,
  testEnvironment: "browserstack",
  globals: {
    browserstack: {
      driver: "@jest-environment-browserstack/selenium-webdriver",
      capabilities: {
        ...browser,
        "bstack:options": {
          ...os,
          buildName: "Valpas local",
          sessionName: [
            browser.browserName,
            browser.browserVersion,
            os.os,
            os.osVersion,
          ].join(" "),
          local: true,
        },
      },
      localTesting: {
        verbose: true,
      },
    },
  },
}
