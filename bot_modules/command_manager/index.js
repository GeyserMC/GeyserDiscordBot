const swearFilter = require('../swear_filter/index.js')

const commands = {}
const aliases = {}

exports.prefix = '!'

/**
 * Handle a message as a command
 *
 * @param {String} command Command to handle
 * @param {String[]} args Arguments to pass
 */
exports.handleCommand = (msg, command, args) => {
  let commandObj

  if (command in commands) {
    commandObj = commands[command]
  }

  if (command in aliases) {
    commandObj = commands[aliases[command]]
  }

  if (commandObj) {
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
 * Register an alias for a command for future use
 *
 * @param {String} alias Alias to register
 * @param {String} target The target command to assign the alias to
 */
exports.registerAlias = (alias, target) => {
  if (target in commands) {
    aliases[alias] = target
  } else {
    console.error(`Cannot register alias '${alias}' for unknown command '${target}'`)
  }
}

/**
 * Get all commands
 *
 * @return {Object} List of all commands registered
 */
exports.getCommands = () => {
  return commands
}
