# FriendesNPCs

Plugin de NPCs que parecen jugadores reales para Minecraft Java y Bedrock.

## Características

- NPCs con apariencia de jugador real (skin, nombre, modelo humanoide)
- Soporte opcional de SkinsRestorer
- Hologramas con TextDisplay entities
- Acciones al hacer clic (mensajes, comandos)
- Totalmente independiente (sin dependencias obligatorias)

## Comandos

- `/fnpc create <nombre> [skin]` - Crear NPC
- `/fnpc remove <id|nombre>` - Eliminar NPC
- `/fnpc list` - Listar NPCs
- `/fnpc rename <id> <nuevo>` - Renombrar
- `/fnpc skin <id> <jugador>` - Cambiar skin
- `/fnpc hologram <add|remove|clear> <id> [texto]`
- `/fnpc action <add|clear> <id> <mensaje>`
- `/fnpc info <id>` - Información
- `/fnpc reload` - Recargar config

## Compilar

```bash
mvn clean package