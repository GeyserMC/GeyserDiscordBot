const { handleWikiCommand } = require('./wiki')
const { handleProviderCommand } = require('./providers')

exports.init = async (client) => {
  client.on('message', async (msg) => {
    // Check the message is clean, not super efficient but
    // the best we can do without rewriting a tonne of code
    if (require('../swear_filter/index.js').checkMessage(msg.content) != null) {
      return
    }

    const args = msg.content.split(' ')

    // Run the relevent function for the command
    switch (args[0]) {
      case '!wiki':
        await handleWikiCommand(msg, args)
        break
      case '!provider':
        await handleProviderCommand(msg, args)
        break
    }
  })
}
