module.exports = {
  transform: {
    "^.+\\.tsx?$": [
      "@swc/jest",
      {
        jsc: {
          parser: { syntax: "typescript", tsx: true },
          target: "es2022",
        },
      },
    ],
  },
  moduleNameMapper: {
    ".*\\.less$": "<rootDir>/test/mocks/styleMock.js",
  },
  snapshotResolver: "<rootDir>/test/snapshotResolver.js",
  testEnvironment: "jsdom",
  setupFilesAfterEnv: ["jest-expect-message", "<rootDir>/test/setup.js"],
  testSequencer: "<rootDir>/test/chunkingSequencer.js",
}
