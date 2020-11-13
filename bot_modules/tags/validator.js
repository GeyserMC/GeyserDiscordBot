// This file is used to make sure all tags are valid

const tags = require('./tags')

console.log('Validating all tags...')
try {
  tags.initTags()
  console.log('All tags validated')
  process.exit(0)
} catch (e) {
  console.log('1 or more tags failed to validate!')
  console.error(e)
  process.exit(1)
}
