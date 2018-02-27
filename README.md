# asteriskClient
Cliente Asterisk - Conversão Manager para Json

Registrar Asterisk:
curl -d "host=ipAsterisk" \
 -d "&username=usuarioManager" \
 -d "&password=senhaManager" \
 -d "&port=5038" \
-X POST http://servidor:8880/asterisk-client/service/v1/registerAsterisk

Registrar Eventos:
curl -d "description=Teste" \
 -d "&url=https://demo6896181.mockable.io" \
 -d "&event=RegistryEvent" \
 -d "&event=PeerStatusEvent" \
 -d "&event=QueueMemberStatusEvent" \
-X POST http://servidor:8880/asterisk-client/service/v1/registerReceiver
