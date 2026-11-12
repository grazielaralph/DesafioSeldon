# Protocolo de Decisões Seldon Fleet

Este documento cumpre o **REQ-10** da especificação técnica do desafio Seldon Fleet, detalhando a regra de classificação adotada, o tratamento concedido a cada um dos defeitos presentes nos dados da telemetria (Seção 3.3) e a análise de ameaças à validade das conclusões.

---

## 1. Regra de Classificação Adotada e Raciocínio

A classificação da saúde dos dispositivos da frota é realizada na camada de Domínio (`domain/`), operando em Kotlin puro sem dependências do Android.

### 1.1 — Normalização e Derivadas
Antes da classificação, calculam-se duas grandezas derivadas:
1. **Dreno Absoluto ($mAh/h$):**
   $$\text{drenoAbsoluto} = \frac{\text{energiaMah}}{\text{screenOnMinutes} / 60}$$
2. **Dreno Relativo ($\%/h$):**
   $$\text{drenoRelativo} = \left( \frac{\text{drenoAbsoluto}}{\text{batteryCapacityMah}} \right) \times 100$$

**Raciocínio:** O **dreno relativo** é a métrica adotada para a tomada de decisão. Comparar apenas o dreno absoluto entre dispositivos de capacidades distintas gera distorção: consumir $900\text{ mAh/h}$ é um estado catastrófico numa bateria de $3000\text{ mAh}$ ($30\%/h$), mas apenas modesto numa bateria de $5000\text{ mAh}$ ($18\%/h$).

### 1.2 — Limiares e Ordem de Precedência
A avaliação segue rigorosamente a ordem abaixo. O primeiro critério satisfeito define o estado final:

| Status no Domínio | Rótulo na UI | Condições de Disparo (Qualquer uma basta) | Significados |
| :--- | :--- | :--- | :--- |
| **`INDETERMINADO`** | **`SEM DADOS`** | `batteryCapacityMah <= 0` ou ausente <br> `screenOnMinutes <= 0` ou ausente <br> `energyUnit == mWh` sem `nominalVoltage` válida | Dados insuficientes para cálculo seguro de dreno |
| **`CRÍTICO`** | **`CRÍTICO`** | `drenoRelativo >= 12.0 %/h` <br> `crashCount7d >= 3` <br> `jankFramesPct >= 15.0 %` | Exige intervenção técnica imediata |
| **`ATENÇÃO`** | **`ATENÇÃO`** | `drenoRelativo >= 7.0 %/h` <br> `crashCount7d >= 1` <br> `jankFramesPct >= 8.0 %` | Sob observação operacional |
| **`OK`** | **`OK`** | Nenhuma das condições anteriores | Dispositivo saudável |

---

## 2. Tratamento dos Defeitos dos Dados (Seção 3.3)

### Defeito 1 — Unidade de Energia Inconsistente (`mWh` vs `mAh`)
- **Problema:** Parte dos registros exporta `energyConsumed` em `mWh` e parte em `mAh`. Comparar ou somar os dois sem converter gera um erro de magnitude de aproximadamente $3.85\times$.
- **Decisão e Tratamento:** No mapeamento para o domínio (`EnergyNormalizer.toMah`), todo valor em `mWh` é convertido para `mAh` utilizando a tensão nominal:
  $$\text{energyMah} = \frac{\text{energyMwh}}{\text{nominalVoltage}}$$
  Registros em `mWh` sem `nominalVoltage` válida são marcados com `energyMah = null` e classificados como `INDETERMINADO`.

### Defeito 2 — Registros Incompletos (Divisor Nulo / Capacidade 0)
- **Problema:** Dispositivos com `batteryCapacityMah = 0` (ou ausente) e `screenOnMinutes = 0` causariam divisão por zero nas derivadas, produzindo `NaN` ou `Infinity`.
- **Decisão e Tratamento:** Adotou-se o tratamento explícito de **classificação como INDETERMINADO** (`SEM DADOS` na UI). O domínio detecta divisores nulos/inválidos antes de calcular o dreno e interrompe a avaliação sem lançar exceção ou expor valores numéricos inválidos na interface.

---

## 3. Ameaça à Validade (REQ-10)

### 3.1 — Suposição de Tela Ativa vs. Dreno em Segundo Plano
A fórmula do dreno relativo assume que todo o consumo de energia registrado (`energyConsumed`) ocorreu exclusivamente durante o tempo em que a tela esteve ativa (`screenOnMinutes`). 

Se um dispositivo executar serviços pesados em primeiro plano (*foreground services*), tarefas de sincronização em segundo plano ou apresentar falhas no modo *Doze* enquanto a tela estiver desligada, o valor de `energyConsumed` será alto mesmo para um tempo de tela baixo. Isso inflará artificialmente o `drenoRelativo`, podendo gerar **falsos positivos de status CRÍTICO** para aparelhos cujo comportamento com tela acesa era normal.

### 3.2 — Limiares Estáticos em Frota Heterogênea
Os limiares de $12\%/h$ e $7\%/h$ são estáticos para toda a frota. Em aparelhos de *tier* de entrada (*low tier*), o processador opera com menor eficiência energética sob carga, fazendo com que drenos mais altos sejam normais para a categoria. Aparelhos de *high tier* com telas de alta taxa de atualização podem apresentar padrões de dreno distintos sem representar degradação física da bateria.
