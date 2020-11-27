const tags = require('./tags')

exports.init = async (client) => {
  try {
    tags.initTags()
  } catch (e) {
    console.log('An error occured loading the tag system, disabling...')
    console.error(e)
    return
  }

  client.on('message', async (msg) => {
    // Check the message is clean, not super efficient but
    // the best we can do without rewriting a tonne of code
    if (require('../swear_filter/index.js').checkMessage(msg.content) != null) {
      return
    }

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
