const Discord = require('discord.js')
const fs = require('fs')
const client = new Discord.Client()

if (!fs.existsSync('.env')) {
  console.error('.env file missing! Closing!')
  process.exit(1)
}

// Load in the .env file
require('dotenv').config()

const modules = {}

function initialiseModule (name) {
  const module = require('./bot_modules/' + name + '/index.js')

  // Check if there is an init and run it
  if ('init' in module && module.init instanceof Function) {
    module.init(client)
  }

  // Register any commands
  if ('commands' in module && 'command_manager' in modules) {
    for (const command of module.commands) {
      modules.command_manager.registerCommand(command)
    }
  }

  modules[name] = module
}

client.on('ready', () => {
  console.log(`Logged in as ${client.user.tag}!`)

  // Load the modules after the login

  // Handle important or long running init first
  initialiseModule('command_manager')
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
  initialiseModule('help_command')
})

client.login(process.env.BOT_TOKEN)
