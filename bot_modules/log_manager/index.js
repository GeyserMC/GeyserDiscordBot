const Discord = require('discord.js')

let logBuffer = ''
let errorBuffer = ''

exports.init = function (client) {
  const webhook = new Discord.WebhookClient(process.env.LOGGER_WEBHOOK_ID, process.env.LOGGER_WEBHOOK_TOKEN)

  process.on('unhandledRejection', (reason) => {
    exports.error('Unhandled promise rejection')
    exports.error(reason.stack)
  })

  setInterval(function () { // Poll the buffer for changes occasionally to allow messages to be bundled
    let finalMessage = ''
    if (logBuffer !== '') {
      finalMessage += '```\n'
      logBuffer.split('\n').forEach(function (line) {
        finalMessage += '  ' + line + '\n'
      })
      finalMessage += '```\n'
      logBuffer = ''
    }
    if (errorBuffer !== '') {
      finalMessage += '```diff\n'
      errorBuffer.split('\n').forEach(function (line) {
        finalMessage += '- ' + line + '\n'
      })
      finalMessage += '```\n'
      errorBuffer = ''
    }
    if (finalMessage !== '') {
      webhook.send(finalMessage)
    }
  }, 500)
}

exports.log = function (message) {
  console.log(message)
  logBuffer += message
}

exports.error = function (message) {
  console.error(message)
  errorBuffer += message
}
