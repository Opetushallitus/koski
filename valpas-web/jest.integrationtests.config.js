const baseConfig = require("./jest.config")

module.exports = {
  ...baseConfig,
  globalSetup: "<rootDir>/test/integrationtests-env/setup.js",
  globalTeardown: "<rootDir>/test/integrationtests-env/teardown.js",
}
