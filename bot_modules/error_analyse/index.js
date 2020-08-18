const Discord = require('discord.js')
const stackParser = require('java-stack-parser')
const Utils = require('../../utils')

const { configEditor: config } = require('../config_manager/index.js')

exports.init = (client) => {
  client.on('message', async (msg) => {
    const configExceptions = config.get().exceptions
    const configExceptionChecks = config.get().exceptionChecks

    configExceptionChecks.forEach(async (exceptionCheck) => {
      const match = msg.content.match(new RegExp(exceptionCheck.regex))
      if (match === null || !match[1]) {
        return
      }

      // Get the hastebin content
      const { status, data: contents } = await Utils.getContents(exceptionCheck.rawUrl.replace('{0}', match[1]))

      // Make sure we got a response
      if (contents === '' || status !== 200) {
        return
      }

      // Parse the log for errors
      const stack = new stackParser.Stack()
      stack.parse(contents)

      // Setup the embed
      const embed = new Discord.MessageEmbed()
      let foundExceptions = false

      // Loop each stacktrace and find any exceptions
      stack.groups.forEach((group) => {
        if (group.exception) {
          foundExceptions = true

          // Build the exception title and see if we have a fix
          const exceptionTitle = group.exception.exception + ': ' + group.exception.message
          const exeption = configExceptions.find(x => exceptionTitle.startsWith(x.error))

          // Check we dont already know about the exception in this log and add it
          if (exeption && !embed.fields.find(x => x.name === exceptionTitle)) {
            embed.addField(exceptionTitle, exeption.message)
          }
        }
      })

      if (foundExceptions) {
        embed.setTitle('Found errors in log!')
        embed.setColor(0xff0000)

        msg.channel.send(embed)
      }
    })
  })
}
