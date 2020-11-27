const Discord = require('discord.js')
const fs = require('fs')

const Utils = require('../../utils')
const { configEditor: config } = require('../config_manager/index.js')

const listFile = 'profanity_filter.wlist'
const listAllowedFile = 'profanity_filter_allowed.wlist'

let bannedWordsRegex = []

exports.init = (client) => {
  // Check the list file exists
  if (!fs.existsSync(listFile)) {
    console.log(`Profanity word list file missing, not loading! (${listFile})`)
    return
  }

  // Fetch the log channel by id
  let logChannel
  client.channels.fetch(config.get().logChannel).then(channel => {
    logChannel = channel
  })

  // Load the word list and build regex expressions from it
  const bannedWords = {}
  fs.readFileSync(listFile, { encoding: 'utf-8' }).split('\n').forEach(word => {
    const newWord = word.toLowerCase().trim()
    if (!(newWord in bannedWords)) {
      bannedWords[newWord] = new RegExp(`(^| )${Utils.escapeRegExp(newWord)}( |$)`, 'i')
    }
  })

  // Load the allowed list and remove them from the banned list
  if (fs.existsSync(listAllowedFile)) {
    fs.readFileSync(listAllowedFile, { encoding: 'utf-8' }).split('\n').forEach(word => {
      const newWord = word.toLowerCase().trim()
      delete bannedWords[newWord]
    })
  }

  // Log the amount of words loaded
  bannedWordsRegex = Object.values(bannedWords)
  console.log(`Loaded ${bannedWordsRegex.length} banned words`)

  function handleMessage (msg) {
    const wordRegex = exports.checkMessage(msg.content)
    if (wordRegex != null) {
      msg.delete().then(msg => {
        msg.reply('your message has been removed because it contains profanity! Please read our rules for more information.')

        const embed = new Discord.MessageEmbed()
        embed.setTitle('Profanity removed')
        embed.setDescription(`**Sender:** ${msg.author}\n**Channel:** ${msg.channel}\n**Regex:** \`${wordRegex}\`\n**Message:** ${msg.content}`)
        embed.setColor(0xff0000)
        logChannel.send(embed)
      }).catch(err => {
        const embed = new Discord.MessageEmbed()
        embed.setTitle('Profanity failed to be removed')
        embed.setDescription(`**Error:** ${err.message}\n**Channel:** ${msg.channel}\n**Sender:** ${msg.author}\n**Regex:** \`${wordRegex}\`\n**Message:** ${msg.content}`)
        embed.setColor(0xff0000)
        logChannel.send(embed)

        console.error('Failed to delete message: ' + err.message)
      })
    }
  }

  // Check on new messages
  client.on('message', async (msg) => {
    handleMessage(msg)
  })

  // Check on message edits
  client.on('messageUpdate', async (oldMsg, newMsg) => {
    handleMessage(newMsg)
  })
}

/**
 * Check if a message is safe to display
 * 
 * @param {Discord.Message} content Content to check
 */
exports.checkMessage = (content) => {
  // We get both to catch more
  const cleanedContent = Utils.cleanText(content)
  const normalisedContent = Utils.normaliseText(cleanedContent)

  // Loop the list looking for a regex match
  // This isn't super efficient but it is fast enough
  for (const wordRegex of bannedWordsRegex) {
    if (cleanedContent.match(wordRegex) || normalisedContent.match(wordRegex)) {
      return wordRegex
    }
  }

  return null
}