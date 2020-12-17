const swearFilter = require('../swear_filter/index.js')

const commands = {}

exports.prefix = '!'

/**
 * Handle a message as a command
 *
 * @param {String} command Command to handle
 * @param {String[]} args Arguments to pass
 */
exports.handleCommand = (msg, command, args) => {
  if (command in commands) {
    const commandObj = commands[command]
    if ('canRun' in commandObj && commandObj.canRun instanceof Function && !commandObj.canRun(msg)) {
      return
    }

    commandObj.run(msg, args)
  }
}

exports.init = (client) => {
  // Check on new messages
  client.on('message', async (msg) => {
    // Check the message is clean, not super efficient but
    // the best we can do without rewriting a tonne of code
    if (swearFilter.checkMessage(msg.content) != null) {
      return
    }

    // Check if the message could be a command and try to handle it
    if (msg.content.startsWith(this.prefix)) {
      const args = msg.content.split(' ')
      const command = args[0].substring(this.prefix.length)
      args.splice(0, 1)
      this.handleCommand(msg, command, args)
    }
  })
}

/**
 * Register a command for future use
 *
 * @param {Object} command Command to register
 */
exports.registerCommand = (command) => {
  commands[command.name] = command
}

/**
 * Get all commands
 *
 * @return {Object} List of all commands registered
 */
exports.getCommands = () => {
  return commands
}
