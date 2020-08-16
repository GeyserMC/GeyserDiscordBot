const { handleWikiCommand } = require('./wiki')
const { handleProviderCommand } = require('./providers')

exports.init = async (client) => {
  client.on('message', async (msg) => {
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
