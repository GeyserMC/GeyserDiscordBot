const Discord = require('discord.js')
const stackParser = require('java-stack-parser')
const Utils = require('../../utils')
const sprintf = require('sprintf-js').sprintf
const path = require('path')

const { configEditor: config } = require('../config_manager/index.js')
const configExceptionFixes = config.get().exceptionFixes
const configExceptionChecks = config.get().exceptionChecks

exports.init = (client) => {
  client.on('message', async (msg) => {
    let foundExceptions = false

    for (const exceptionCheck of configExceptionChecks) {
      const match = msg.content.match(new RegExp(exceptionCheck.regex))
      // Make sure we matched atleast 1 group
      if (match === null || !match[1]) {
        continue
      }

      // Remove the first part of the match
      match.splice(0, 1)

      // Get the log content
      const { status, data: contents } = await Utils.getContents(sprintf(exceptionCheck.rawUrl, ...match))

      // Make sure we got a response
      if (contents === '' || status !== 200) {
        continue
      }

      foundExceptions = parseLog(msg, contents)
    }

    // If we have already found exceptions stop
    if (foundExceptions) {
      return
    }

    // Check attachments for logs
    for (const [, attachment] of msg.attachments) {
      if (config.get().exceptionExtensions.includes(path.extname(attachment.name))) {
        // Get the log content
        const { status, data: contents } = await Utils.getContents(attachment.url)

        // Make sure we got a response
        if (contents === '' || status !== 200) {
          continue
        }

        foundExceptions = parseLog(msg, contents)
      }
    }

    // If we have already found exceptions stop
    if (foundExceptions) {
      return
    }

    // Check the message for any errors
    parseLog(msg, msg.content)
  })
}

function parseLog (msg, contents) {
  // Get the branch from the log if its there
  let branch = 'master'
  const branchMatch = contents.match(/Geyser .* \(git-[0-9a-zA-Z]+-([0-9a-zA-Z]{7})\)/)
  if (branchMatch !== null && branchMatch[1]) {
    branch = branchMatch[1]
  }

  // Parse the log for errors
  const stack = new stackParser.Stack()
  stack.parse(contents)

  // Setup the embed
  const embed = new Discord.MessageEmbed()
  let geyserException = false

  // Break the stack down into a better format
  // incase there are multiple errors
  const exceptions = []
  stack.groups.forEach((group) => {
    if (group.exception) {
      exceptions.push({
        exception: group.exception.exception,
        message: group.exception.message,
        lines: group.lines,
        packages: [group.stackPackage]
      })
    } else {
      const exception = exceptions[exceptions.length - 1]
      if (exception !== undefined) {
        exception.lines = exception.lines.concat(group.lines)
        exception.packages.push(group.stackPackage)
      }
    }
  })

  if (exceptions.length <= 0) {
    return false
  }

  // Loop each stacktrace and find any exceptions
  exceptions.forEach((exception) => {
    let currentGeyserException = false

    // Check if we found a GeyserMC package in the stack trace
    for (const stackPackage of exception.packages) {
      if (stackPackage.name.startsWith('org.geysermc') && !stackPackage.name.includes('shaded')) {
        geyserException = true
        currentGeyserException = true
        break
      }
    }

    // Build the exception title and see if we have a fix
    const exceptionTitle = exception.exception + ': ' + exception.message
    const exceptionFix = configExceptionFixes.find(x => exceptionTitle.startsWith(x.error))

    // Check we dont already know about the exception in this log and add it
    if (exceptionFix && !embed.fields.find(x => x.name === exceptionTitle)) {
      embed.addField(exceptionTitle, exceptionFix.message)
    } else if (currentGeyserException) {
      // Find the first line causing the error in the org.geyser package
      for (const line of exception.lines) {
        if (line.stackPackage.name.startsWith('org.geysermc') && !line.stackPackage.name.includes('shaded')) {
          // Get the package url
          const packageBreakdown = line.stackPackage.name.split('.')
          let submodule = packageBreakdown[2]
          if (submodule === 'platform') {
            submodule = 'bootstrap/' + packageBreakdown[3]
          }

          let repo = 'Geyser'

          if (submodule === 'floodgate') {
            branch = 'development'
            submodule = 'common' // This will likely become dynamic after the floodgate rewrite
            repo = 'Floodgate'
          }

          // Work out the url for the error
          const url = `https://github.com/GeyserMC/${repo}/blob/${branch}/${submodule}/src/main/java/${line.stackPackage.name.replace(/\./g, '/')}/${line.source}#L${line.line}`

          // Build the description
          const exceptionDesc = `Unknown fix!\nClass: \`${line.javaClass}\`\nMethod: \`${line.method}\`\nLine: \`${line.line}\`\nLink: [${line.source}#L${line.line}](${url})`

          // Make sure we dont already have that field
          if (!embed.fields.find(x => x.name === exceptionTitle && x.value === exceptionDesc)) {
            embed.addField(exceptionTitle, exceptionDesc)
          }

          // Add a field with the exception details for debugging
          break
        }
      }
    }
  })

  // Set the base title and description of the embed
  embed.setTitle('Found errors in the log!')
  embed.setDescription('See below for details and possible fixes')
  embed.setColor(0xff0000)

  // If we have no fields set the description acordingly
  if (embed.fields.length <= 0) {
    embed.setDescription('We don\'t currently have automated responses for the detected errors!')
  }

  msg.channel.send(embed)

  return true
}
