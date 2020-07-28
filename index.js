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
  return require('./bot_modules/' + name + '/index.js').init(client, config)
}

client.on('ready', () => {
  console.log(`Logged in as ${client.user.tag}!`)
})

// Load the config
const config = require('./bot_modules/config_manager/index.js').init(client)

// Load the modules
initialiseModule('dump_analyse')

client.login(process.env.BOT_TOKEN)
