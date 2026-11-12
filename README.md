# Seldon Fleet — Monitoramento de Frota Android

Aplicação Android desenvolvida em **Kotlin** e **Jetpack Compose** para leitura, classificação de saúde e monitoramento de telemetria de uma frota de dispositivos Android a partir de dados locais (`assets/frota.json`).

---

## 📱 Capturas de Tela (Screenshots)

| Tela de Lista da Frota | Detalhe do Dispositivo | Filtro e Favoritos |
| :---: | :---: | :---: |
| <img src="files/WhatsApp%20Image%202026-11-12%20at%2018.35.51.jpeg" width="280" alt="Tela de Lista" /> | <img src="files/WhatsApp%20Image%202026-11-12%20at%2018.35.52.jpeg" width="280" alt="Detalhe do Dispositivo" /> | <img src="files/WhatsApp%20Image%202026-11-12%20at%2018.35.52%20(1).jpeg" width="280" alt="Filtro e Favoritos" /> |

---

## 🛠️ Como Executar o Projeto

### Pré-requisitos
- Android Studio Ladybug (ou mais recente) / JDK 17
- Android SDK 34 ou superior

### Comandos Gradle no Terminal

```bash
# Executar a suíte de testes unitários na JVM (REQ-08)
./gradlew test

# Executar a análise estática (Lint - REQ-09)
./gradlew lint

# Compilar o APK de Debug
./gradlew assembleDebug
```

---

## 📐 Decisões de Arquitetura e Trade-offs

O projeto segue estritamente os princípios de **Clean Architecture** e **Inversão de Dependência**:

1. **Camada de Domínio (`domain/`):**
   - **Kotlin puro**, sem `import android.*`, sem dependência de frameworks de UI ou parsers JSON.
   - Contém o modelo `Device`, o enum `DeviceStatus` (`OK`, `ATENCAO`, `CRITICO`, `INDETERMINADO`), a abstração `DeviceRepository` e a regra de negócio isolada em `ClassifyDevice`.
   - Permite execução dos testes na JVM em milissegundos.

2. **Camada de Dados (`data/`):**
   - Responsável pela leitura de `assets/frota.json` através do Gson DTO (`FleetResponseDto`, `DeviceDto`).
   - Converte unidades físicas em `EnergyNormalizer` (`mWh` -> `mAh` via `nominalVoltage`).
   - Gerencia a persistência dos favoritos de forma assíncrona com `Jetpack DataStore`.

3. **Camada de Apresentação (`presentation/`):**
   - Construída 100% com **Jetpack Compose** e **Material 3**.
   - Gerida pelo `FleetViewModel`, expondo `FleetUiState` reativo através de `StateFlow`.
   - Oferece suporte a filtros interativos com contagem de status e navegação preservando o filtro selecionado.

---

## 🚀 O Que Faria em Seguida

Se este projeto fosse evoluir para um produto de produção, as seguintes etapas seriam implementadas:

1. **Paginação e Filtros Avançados:** Adicionar ordenação por dreno/bateria e barra de pesquisa por modelo ou ID para suportar frotas de milhares de aparelhos.
2. **Classificação Dinâmica por Tier (Trilha A):** Substituir limiares estáticos por percentis calculados por categoria de SoC (*low*, *mid*, *high*) para reduzir falsos positivos.
3. **Mapeamento de Rede & WorkManager (Trilha C):** Implementar ingestão remota via API REST/Retrofit com sincronização periódica em segundo plano via `WorkManager`.
4. **Testes de UI e Screenshot Testing:** Criar testes instrumentados em Compose para validar interações visuais e navegação.

---

## 🤖 Declaração de Uso de IA (Seção 11)

Em conformidade com a política de transparência do desafio:
- **Ferramentas Utilizadas:** Assistente AI do Android Studio e gemini-3.1-pro-preview
- **Contribuição da IA:** Utilizada no auxílio de geração de boilerplate de UI em Compose, formatação de arquivos Markdown e estruturação de casos de testes unitários.
- **Raciocínio Humano:** A definição das decisões de arquitetura em camadas, regras de precedência da classificação do domínio, limites de tolerância e tratamento dos defeitos de dados da telemetria foram concebidos e validados diretamente pelo desenvolvedor responsável.
