# Protocolo Seldon Fleet

Este documento descreve as regras de negócio adotadas para a classificação da frota, o histórico de tratamento de defeitos encontrados durante o desenvolvimento e as ameaças à validade da solução implementada, cumprindo o REQ-10.

## 1. Regra Adotada (Lógica de Classificação)

A classificação da saúde de cada dispositivo da frota é realizada na Camada de Domínio (Clean Architecture), resultando em um de quatro estados possíveis: **OK**, **ATENÇÃO**, **CRÍTICO** ou **INDETERMINADO**. A regra adotada avalia múltiplas métricas do dispositivo de forma sequencial:

1. **Estado CRÍTICO:** Atribuído se o dispositivo apresenta falhas severas de estabilidade (ex: `crashCount7d` acima de um limite aceitável) ou se o nível de bateria (`batteryLevelPct`) está perigosamente baixo comprometendo a operação imediata.
2. **Estado ATENÇÃO:** Atribuído se o dispositivo não é crítico, mas apresenta degradação de performance (ex: alta percentagem de *Jank Frames*) ou um consumo de energia anômalo para o tempo de ecrã ativo (`screenOnMinutes`).
3. **Estado OK:** Atribuído aos dispositivos que operam dentro dos parâmetros normais de estabilidade, bateria e performance, sem violar as regras anteriores.
4. **Estado INDETERMINADO:** Fallback utilizado caso as métricas cruciais para a avaliação venham zeradas, nulas ou num formato inconsistente, impedindo um diagnóstico seguro.

Cada classificação gera um objeto `Decision` que contém o status e uma `reason` (motivo descritivo), garantindo transparência na interface de detalhes.

## 2. Tratamento dos Defeitos

Durante a integração das camadas e construção da UI em Jetpack Compose, os seguintes defeitos foram identificados e mitigados:

* **Defeito 1: Dependências e Referências Não Resolvidas**
  * **Sintoma:** Falhas de compilação apontando `Unresolved reference` para `Gson`, anotações e ícones do Material Design.
  * **Tratamento:** As bibliotecas não vêm por padrão nos templates modernos do Android Studio. O defeito foi corrigido adicionando `com.google.code.gson:gson` e `androidx.compose.material:material-icons-extended` ao `build.gradle.kts` e sincronizando o projeto.

* **Defeito 2: Ecrã em Branco (Falha de Parsing do JSON)**
  * **Sintoma:** A interface carregava corretamente, mas a lista de dispositivos estava vazia (Lista Vazia retornada pelo Repositório).
  * **Tratamento:** O código inicial assumia que `frota.json` era um *Array* direto (`[ ... ]`). Contudo, o JSON era um *Objeto* (`{ ... }`) contendo metadados e a lista estava na propriedade `"devices"`. Criou-se um Wrapper/Envelope (`FleetResponseDto`) para ler o objeto raiz e extrair a lista corretamente, evitando a exceção silenciosa do Gson.

* **Defeito 3: Falsos Positivos "INDETERMINADO" e Falha no Clique**
  * **Sintoma:** Todos os cartões renderizavam com status "INDETERMINADO", e as propriedades vitais (como `id`) estavam nulas, impedindo a navegação para os detalhes.
  * **Tratamento:** Incompatibilidade tipográfica entre as chaves no JSON e as anotações `@SerializedName` na classe `DeviceDto`. As chaves foram corrigidas para respeitar rigorosamente o *camelCase* do JSON (ex: `ramMb` em vez de outro formato). Com os dados devidamente mapeados, o domínio conseguiu calcular as regras (saindo do fallback) e a UI obteve os IDs válidos.

* **Defeito 4: Falha na Navegação de Ecrãs (Compose State)**
  * **Sintoma:** A `MainActivity` não conseguia alternar entre a `FleetScreen` e a `DeviceDetailScreen`.
  * **Tratamento:** A navegação dependia de estados que não estavam a ser geridos corretamente. Implementou-se uma variável `selectedDeviceId` suportada por `mutableStateOf` e `remember`, juntamente com os imports manuais das funções de estado do Compose. A UI passou a reagir dinamicamente a essa variável para desenhar o ecrã correspondente.

## 3. Ameaça à Validade (REQ-10)

Embora a solução implementada atenda aos requisitos funcionais primários, existem limitações contextuais que representam ameaças à sua validade num cenário de produção real:

1. **Regras Estáticas vs. Frota Heterogênea:** As regras de classificação de bateria e performance utilizam limiares estáticos (hardcoded). Como a frota é composta por dispositivos de diferentes tiers (*low*, *mid*, *high*) e capacidades de bateria muito díspares, um consumo aceitável para um *Pixel 8* pode ser considerado crítico para um *Moto E22*. A regra estática ameaça a precisão do diagnóstico (falsos positivos/negativos).
2. **Dados Estáticos (Offline):** A aplicação lê os dados a partir de um ficheiro local (`assets/frota.json`). Num ambiente real de telemetria, as métricas dos dispositivos mudam constantemente. A arquitetura atual precisaria de ser adaptada (ex: Retrofit/WebSockets) para consumir uma API remota, sob o risco de os diagnósticos apresentados estarem obsoletos no momento da leitura.
3. **Escalabilidade da Interface:** O ecrã inicial renderiza os dispositivos numa `LazyColumn`. Contudo, para frotas massivas (ex: milhares de dispositivos), a ausência de mecanismos de paginação, filtros de pesquisa ou ordenação ameaça a usabilidade da aplicação e a capacidade do utilizador de encontrar rapidamente os telemóveis em estado crítico.