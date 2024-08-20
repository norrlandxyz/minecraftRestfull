# minecraftRestfull

Change auth-token in config.yml

## Endpoints
- `/_minecraftRestfull/bukkit/dispatchCommand/` (send command in body)
- `/_minecraftRestfull/bukkit/setWhitelisted/{true/false}/{player_uuid}`
- `/_minecraftRestfull/bukkit/isWhitelisted/{player_uuid}`
- `/_minecraftRestfull/bukkit/getWhitelistedPlayers`

- `/_minecraftRestfull/vault/playerAddGroup/{player_uuid}/{group_name}`
- `/_minecraftRestfull/vault/playerRemoveGroup/{player_uuid}/{group_name}`
- `/_minecraftRestfull/vault/getPlayerGroups/{player_uuid}`

## Example Usage
`curl -X GET http://localhost:7070/_minecraftRestfull/vault/getPlayerGroups/aaa9-63c7-411b-9601-45e13bd4ce42 -H 'auth-token:  YOUR_SECRET_TOKEN'`
`curl -X GET http://localhost:7070/_minecraftRestfull/bukkit/dispatchCommand/ -d "say Hello World!" -H 'auth-token:  YOUR_SECRET_TOKEN'`


## Dependencies
Vault
