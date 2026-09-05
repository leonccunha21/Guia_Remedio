# ZM Remédio Certo

Aplicativo Android para organizar medicamentos, horários, estoque e histórico de doses. Desenvolvido em Kotlin com Jetpack Compose, Room, Hilt e Firebase.

> O aplicativo é uma ferramenta de organização. Informações geradas por IA não substituem médico, farmacêutico ou a bula oficial.

## Recursos

- Cadastro de medicamentos e perfis familiares
- Alarmes com confirmação e adiamento de dose
- Histórico, relatório PDF e indicador de adesão
- Estoque e alertas de reposição
- Leitura de embalagem por câmera (OCR)
- Consulta assistida por IA com chave do próprio usuário
- Login e backup Firebase para contas identificadas
- Proteção biométrica e widget Android
- Dados locais isolados por conta Firebase
- Restauração automática do backup em aparelhos novos
- Tratamentos com período de início e término
- Linha do tempo diária com doses tomadas, atrasadas, adiadas e ignoradas
- Diário de sintomas, pressão arterial, glicemia e bem-estar
- Modo cuidador com alertas remotos dentro do aplicativo

## Requisitos

- Android Studio compatível com AGP 8.7
- JDK 11
- Android SDK 35
- Projeto Firebase configurado em `app/google-services.json`

## Executar

```bash
./gradlew assembleDebug
```

No Windows, use `gradlew.bat assembleDebug`.

## Qualidade

```bash
./gradlew testDebugUnitTest lintDebug
```

## Estrutura

- `data/local`: banco Room e DAO
- `data/remote`: backup Firebase
- `data/repository`: regras de acesso a dados e autenticação
- `ui`: telas Compose e estado da aplicação
- `receiver`: alarmes, ações de notificação e restauração no boot
- `util`: PDF, voz e agendamento

## Privacidade

O backup automático do Android está desativado porque o banco pode conter informações de saúde. Dados Room são separados pelo UID autenticado, a chave da IA usa armazenamento criptografado e o backup Firebase é gravado sob `users/{uid}`. Publique também as regras presentes em `regras/database.rules.json` no Firebase Console.

## Licença

O repositório ainda não declara uma licença. Adicione uma antes de distribuir ou aceitar contribuições externas.
