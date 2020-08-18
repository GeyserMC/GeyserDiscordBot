const Discord = require('discord.js')
const stackParser = require('java-stack-parser')
const Utils = require('../../utils')
const sprintf = require('sprintf-js').sprintf
const path = require('path')

const { configEditor: config } = require('../config_manager/index.js')
const configExceptions = config.get().exceptions
const configExceptionChecks = config.get().exceptionChecks

exports.init = (client) => {
  client.on('message', async (msg) => {
    let foundExceptions = false

    configExceptionChecks.forEach(async (exceptionCheck) => {
      const match = msg.content.match(new RegExp(exceptionCheck.regex))
      // Make sure we matched atleast 1 group
      if (match === null || !match[1]) {
        return
      }

      // Remove the first part of the match
      match.splice(0, 1)

      // Get the log content
      const { status, data: contents } = await Utils.getContents(sprintf(exceptionCheck.rawUrl, ...match))

      // Make sure we got a response
      if (contents === '' || status !== 200) {
        return
      }

      foundExceptions = parseLog(msg, contents)
    })

    // If we have already found exceptions stop
    if (foundExceptions) {
      return
    }

    // Check attachments for logs
    msg.attachments.forEach(async (attachment) => {
      if (config.get().exceptionExtensions.includes(path.extname(attachment.name))) {
        // Get the log content
        const { status, data: contents } = await Utils.getContents(attachment.url)

        // Make sure we got a response
        if (contents === '' || status !== 200) {
          return
        }

        foundExceptions = parseLog(msg, contents)
      }
    })

    // If we have already found exceptions stop
    if (foundExceptions) {
      return
    }

    // Check the message for any errors
    parseLog(msg, msg.content)
  })
}

function parseLog (msg, contents) {
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
}
