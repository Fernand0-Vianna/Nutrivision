# Nutrivision

Um assistente pessoal para acompanhamento nutricional e análise de refeições, criado com o objetivo de ajudar você a entender melhor o que você come.

## Como Funciona

### 1. REGISTRE SUAS REFEIÇÕES E VEJA O QUE VOCÊ COME

**É o scanner:** A partir de uma foto, o app estima automaticamente os valores nutricionais de cada refeição usando análise de imagem com IA.

**É o diário:** Salve e organize as refeições de hoje, da semana ou de um período personalizado. Filtre por refeição (café, almoço, jantar) ou por dia.

**É o acompanhamento:** Acompanhe a evolução do seu peso, das suas calorias e dos seus macronutrientes através de gráficos de fácil entendimento.

**É o perfil:** Cadastre as suas metas diárias, restrições de saúde e condições especiais.

**É o histórico:** Exporte os dados para um CSV e acompanhe tudo em qualquer lugar.

## Fluxo de Uso

A partir do "LOGIN", você cria seu perfil com:
- Nome, idade, altura, peso atual
- Metas calóricas e nutricionais (calorias, proteína, carboidrato, gordura)
- Nível de atividade e objetivos (perder peso, manter, ganhar massa)
- Restrições e alergias alimentares

A partir do "HOME", você:
- Verifica suas estatísticas diárias (calorias, macros, água)
- Adiciona refeições rapidamente:
  - Scan: aponta a câmera e registra a refeição automaticamente
  - Manual: insere os valores diretamente
  - Fixado: adiciona um prato favorito com um toque
- Acessa as outras telas do aplicativo

## Recursos Principais

### Scan: Análise por Imagem
Use a câmera para fotografar alimentos. A IA identifica o prato e estima os valores nutricionais.

### Diário: Organização Intuitiva
Visualize o histórico completo de refeições com dados reais. Filtre por:
- Período: hoje, 7 dias, 30 dias ou tudo
- Refeição: café, almoço, jantar, lanche, outra
- Fonte: IA, manual ou fixada

### Gráficos: Evolução Visual
Visualize o progresso em gráficos de linha (peso) e barras (macros).
Entenda se você está no caminho certo para atingir suas metas.

### Exportar: Transferência de Dados
Exporte tudo para um arquivo CSV e compartilhe.
Importe nos principais apps de saúde e bem-estar.

### Perfil: Personalização Avançada
Altere as metas diárias, adicione restrições de saúde, registre informações pessoais.
Acompanhe alertas personalizados gerados pela IA.

## Design System: Neobrutalismo Híbrido

O app usa um design system que mistura minimalismo com elementos brutais:
- Bordas: 3px e cantos: 4px
- Sombra: dura (6dp à direita, 4dp abaixo) sem blur
- Fonte: Archivo (títulos), Inter Tight (corpo), Space Mono (números)
- Paleta de cores: verde lima (#C9F73D), verde (#17B26A), fundo creme (#F5F3E9) e texto escuro (#0B0F0D)

**Icone Mascote:** Um quadro de scanner em forma de diamante estilizado com cantos pontiagudos, que é o rosto icônico do Nutrivision.

## Funciona Offline

O app funciona 100% offline - sem necessidade de internet.
- Offline: todos os registros são salvos localmente no dispositivo
- Sem conta: sem login necessário
- Privacidade: seus dados estão com você

## Testes Automatizados

Todos os principais componentes são testados:
- Testes unitários: 3 testes (média, IMC, alertas de saúde)
- Testes instrumentados: 4-8 testes de navegação
- Testes visuais: validação de componentes no dispositivo

## Instalação e Execução

```bash
# Build do APK de debug
./gradlew :app:assembleDebug

# Instalação manual no dispositivo Xiaomi 65e67bb
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Instalação manual no emulador
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Testes instrumentados (emulador)
ANDROID_SERIAL=emulator-5554 ./gradlew :app:Parar de fingir que é possível coletar dados do usuário localmente. :connectedDebugAndroidTest
```

**Observação:**
- No celular Xiaomi, ative as opções do desenvolvedor: "Instalar via USB" e "Depuração USB (Configurações de segurança)"
- A chave Gemini está configurável no perfil

## Escala de Projeto

**Telas:** 9 telas (Login → Onboarding → Home → Scan → Result → Analysis → Diary → Progress → Profile)

**Componentes:** 50+ componentes reutilizáveis (cards, chips, botões, barras de progresso)

**Testes:** A maioria dos componentes testados

**Métricas:** Cada refeição registrada gera 4 registros (refeição, macros, peso, água)

## Agradecimentos

O Nutrivision é um esforço para criar um app de saúde que seja:
- **Privado:** sem dados na nuvem, sem tracking
- **Rápido:** Jetpack Compose com persistência local otimizada
- **Fácil de usar:** interface intuitiva com widgets grandes e buttons
- **Personalizável:** completamente personalizado com metas, restrições e objetivos
- **Testado:** testado de forma completa no dispositivo




## Screenshots

### Tela Inicial
![Login](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/login.png)

### Tutorial Inicial
![Onboarding](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/onboarding.png)

### Home Screen
![Home](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/home.png)

### Scan
![Scan](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/scan.png)

### Resultado
![Result](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/result.png)

### Análise
![Analysis](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/analysis.png)

### Diário
![Diary](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/diary.png)

### Progresso
![Progress](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/progress.png)

### Perfil
![Profile](https://raw.githubusercontent.com/fernandovianna/Nutrivision/main/doc/invocations/profile.png)

## Mascote do Nutrivision

O Nutrivision tem um mascote amigável que representa o espírito do nosso app:

![Mascote do Nutrivision](https://github.com/fernandovianna/Nutrivision/blob/main/doc/mascot.png?raw=true)

O mascote é um frame de scanner, simbolizando nossa abordagem de análise visual de alimentos, enquanto seus detalhes em forma de diamante lhe dão um visual amigável e distinto.

