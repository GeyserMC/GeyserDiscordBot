const { handleWikiCommand } = require('./wiki')
const { handleProviderCommand } = require('./providers')

exports.commands = [
  {
    name: 'wiki',
    args: '<search>',
    description: 'Search the Geyser wiki',
    run: async (msg, args) => {
      await handleWikiCommand(msg, ['wiki', ...args])
    }
  },
  {
    name: 'provider',
    args: '<provider>',
    description: 'Search the Supported Providers page on the Geyser wiki to see if a provider is supported',
    run: async (msg, args) => {
      await handleProviderCommand(msg, ['provider', ...args])
    }
  }
]
