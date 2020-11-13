const path = require('path')
const fs = require('fs')

// Relative to the project root
const tagFolder = './tags'

/**
 * Load the tags from file and return them
 */
exports.load = () => {
  const tags = {}

  // Read tags from files
  fs.readdirSync(tagFolder).forEach(file => {
    if (!file.endsWith('.tag')) {
      return
    }

    // Read the tag contents
    const content = fs.readFileSync(path.join(tagFolder, file), { encoding: 'utf-8' })

    // Get the name and set the base tag
    const tagName = file.substr(0, file.length - 4).toLowerCase()
    const tag = {
      type: 'text',
      target: '',
      content: ''
    }

    // Loop each line
    let hitSeperator = false
    content.split('\n').forEach(line => {
      line = line.trim()

      // Have we hit the contents yet and if so just append
      if (hitSeperator) {
        tag.content += line + '\n'
        return
      }

      // Check for the seperator
      if (line === '---') {
        hitSeperator = true
      } else if (line.length !== 0 && !line.startsWith('#')) {
        // Parse the tag options
        const lineParts = line.split(':')
        switch (lineParts[0]) {
          case 'type':
            tag.type = lineParts[1].trim().toLowerCase()
            break

          case 'target':
            tag.target = lineParts[1].trim().toLowerCase()
            break

          default:
            console.error(`Invalid tag option '${lineParts[0]}' for tag '${tagName}'`)
            break
        }
      }
    })

    tag.content = tag.content.trim()

    tags[tagName] = tag
  })

  return tags
}
