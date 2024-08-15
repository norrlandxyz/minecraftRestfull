# mineraftRestfull

Change auth-token in config.yml

## Endpoints
- `/_minecraftRestfull/bukkit/dispatchCommand/{command}`
- `/_minecraftRestfull/vault/playerAddGroup/{player_uuid}/{group_name}`
- `/_minecraftRestfull/vault/playerRemoveGroup/{player_uuid}/{group_name}`
- `/_minecraftRestfull/vault/getPlayerGroups/{player_uuid}`

## Example Usage
`curl -X GET http://localhost:7070/_minecraftRestfull/bukkit/dispatchCommand/say%20Hello%20World! -H 'auth-token: your_secret_token' -H 'Accept: application/json'`

## Dependencies
Vault
