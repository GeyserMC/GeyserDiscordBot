const tags = require('./tags')

exports.init = async (client) => {
  tags.initTags()

  client.on('message', async (msg) => {
    const args = msg.content.split(' ')

    // Allow for tags to be called with the prefix `!!`
    if (args[0].match(/^!![\w-]+$/)) {
      const newArgs = ['!tag', ...args]
      newArgs[1] = newArgs[1].substr(2)
      await tags.handleTagCommand(msg, newArgs)
      return
    }

    // Run the relevent function for the command
    switch (args[0]) {
      case '!tags':
        await tags.handleTagsCommand(msg, args)
        break
      case '!tag':
        await tags.handleTagCommand(msg, args)
        break
    }
  })
}
