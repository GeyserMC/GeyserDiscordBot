const tags = require('./tags')

const commandManager = require('../command_manager/index.js')
const tagRegex = new RegExp('^' + commandManager.prefix + commandManager.prefix + '[\\w-]+$')

exports.init = async (client) => {
  try {
    tags.initTags()
  } catch (e) {
    console.log('An error occured loading the tag system, disabling...')
    console.error(e)
    return
  }

  client.on('message', async (msg) => {
    const args = msg.content.split(' ')

    // Allow for tags to be called with the prefix `!!`
    if (args[0].match(tagRegex)) {
      args[0] = args[0].substr(2)
      commandManager.handleCommand(msg, 'tag', args)
    }
  })
}

exports.commands = [
  {
    name: 'tags',
    description: 'List all the known (non-alias) tags',
    run: async (msg, args) => {
      await tags.handleTagsCommand(msg, ['tags', ...args])
    }
  },
  {
    name: 'tag',
    args: '<name>',
    description: 'Display a tag for the given name',
    run: async (msg, args) => {
      await tags.handleTagCommand(msg, ['tag', ...args])
    }
  }
]
