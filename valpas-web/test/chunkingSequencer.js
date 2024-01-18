const Sequencer = require("@jest/test-sequencer").default
const path = require("path")

class ChunkingSequencer extends Sequencer {
  sort(tests) {
    const chunking = (process.env.TEST_CHUNK || "").match(/(\d+)\/(\d+)/)
    const sortablePath = (test) => {
      // Take only last 4 parts of the full file path (starting from 'valpas-web') to avoid a dependency on the
      // full directory path that might be different on different test runners.
      return test.path.split(path.sep).slice(-4).join(path.sep)
    }

    const sortedTests = tests.sort((a, b) => {
      return sortablePath(a).localeCompare(sortablePath(b))
    })

    if (chunking) {
      const chunkIndex = parseInt(chunking[1], 10) - 1
      const chunkCount = parseInt(chunking[2], 10)
      const testChunk = sortedTests.filter(
        (_test, index) => index % chunkCount === chunkIndex,
      )

      console.log(
        `Running test chunk ${chunking[0]} (${testChunk.length} of ${sortedTests.length} tests):`,
      )
      testChunk.forEach((test) => console.log(test.path))

      return process.env.LIST_ONLY ? [] : testChunk
    } else {
      return sortedTests
    }
  }
}

module.exports = ChunkingSequencer
