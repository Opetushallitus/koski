module.exports = {
  preset: "ts-jest",
  moduleNameMapper: {
    ".*\\.less$": "<rootDir>/test/mocks/styleMock.js",
  },
  snapshotResolver: "<rootDir>/test/snapshotResolver.js",
  testEnvironment: "jsdom",
  setupFilesAfterEnv: ["jest-expect-message", "<rootDir>/test/setup.js"],
}
