module.exports = {
  preset: "ts-jest",
  testEnvironment: "node",
  moduleNameMapper: {
    ".*\\.less$": "<rootDir>/test/mocks/styleMock.js",
  },
  snapshotResolver: "<rootDir>/test/snapshotResolver.js",
}
