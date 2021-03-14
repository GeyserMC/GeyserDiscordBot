const Discord = require('discord.js')
const fs = require('fs')
const client = new Discord.Client()

if (!fs.existsSync('.env')) {
  console.error('.env file missing! Closing!')
  process.exit(1)
}

// Load in the .env file
require('dotenv').config()

function initialiseModule (name) {
  return require('./bot_modules/' + name + '/index.js').init(client)
}

client.on('ready', () => {
  console.log(`Logged in as ${client.user.tag}!`)

  // Load the modules after the login

  // Handle important or long running init first
  initialiseModule('config_manager')
  initialiseModule('swear_filter')
  initialiseModule('live_updates')
  initialiseModule('tags')

  // Everything else after
  initialiseModule('dump_analyse')
  initialiseModule('restart_command')
  initialiseModule('search_commands')
  initialiseModule('attachment_converter')
  initialiseModule('error_analyse')
  initialiseModule('autopublish')
  initialiseModule('help_command')
})

client.login(process.env.BOT_TOKEN)
