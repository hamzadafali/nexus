# ADR-001 — Choix du modèle LLM pour NEXUS v0.x

- Statut : Proposé — passe en **Accepté** une fois les colonnes « mesuré » remplies (voir Annexe B)
- Date : 2026-10-05
- Décideur : Hamza
- Ticket : NEXUS-01

## Contexte

NEXUS est un copilote AI pour équipes engineering. Les versions v0.1 à v0.3 ont besoin
d'un modèle capable de :

- répondre précisément à des questions techniques (architecture, incidents, runbooks) ;
- produire un structured output fiable (JSON → record Java, via `ChatClient.entity(...)`) ;
- gérer un contexte RAG (question + ~10 chunks + historique, soit **~7,5 k tokens en entrée**, voir estimation ci-dessous) ;
- supporter le tool calling (à partir de NEXUS-19).

Contraintes : budget personnel limité, intégration Spring AI 2.0 (le projet utilise aujourd'hui
`spring-ai-starter-model-openai` avec `gpt-4o-mini`), latence compatible avec un chat.

### Estimation du profil de tokens par question

| Phase | System prompt | Question | Chunks RAG | Historique | Total input | Output |
|---|---|---|---|---|---|---|
| v0.1 – v0.2 (sans RAG) | ~500 | ~100 | — | ~1 000 | **~1,6 k** | ~500 |
| v0.3 (RAG, 10 × ~500 tokens) | ~500 | ~100 | ~5 000 | ~2 000 | **~7,5 k** | ~700 |

Conclusion : une context window de 128 k suffit très largement pour v0.x. La context window
n'est donc **pas un critère discriminant** à ce stade ; le coût et la qualité le sont.

## Critères de décision (par ordre de priorité)

1. Qualité sur les questions NEXUS
2. Fiabilité du structured output et du tool calling
3. Coût par question
4. Latence (p50, time-to-first-token)
5. Context window

## Options envisagées

Shortlist : un modèle intermédiaire et un petit modèle chez le provider actuel (OpenAI),
un modèle intermédiaire chez un second provider (Anthropic), et le modèle actuel comme baseline.

| Critère | **GPT-6.1 Sol** | **GPT-6 Luna** | **Claude Sonnet 5.5** | gpt-4o-mini (baseline actuelle) |
|---|---|---|---|---|
| Model ID | `gpt-6.1-sol` | `gpt-6-luna` | `claude-sonnet-5-5` | `gpt-4o-mini` |
| Gamme | Intermédiaire | Petit / rapide | Intermédiaire | Petit (ancienne génération) |
| Prix input ($/M tokens) | 2,00 | 0,10 | 2,00 | 0,15 |
| Prix input en cache ($/M tokens) | 0,10 | 0,01 | 0,20 | 0,075 |
| Prix output ($/M tokens) | 10,00 | 0,50 | 10,00 | 0,60 |
| Context window | 1,05 M | 1,05 M | 1 M | 128 k |
| Taille max de réponse | 128 k | 128 k | 128 k | 16 k |
| Structured output / tool calling | Oui / Oui | Oui / Oui (¹) | Oui / Oui (²) | Oui / Oui |
| Coût estimé / question v0.3 (7,5 k in / 700 out) | ~0,022 $ | ~0,0011 $ | ~0,022 $ (³) | ~0,0016 $ |
| Coût estimé pour 1 000 questions | ~22 $ | ~1,1 $ | ~22 $ (³) | ~1,6 $ |
| Tokens in/out moyens (mesurés) | _à mesurer_ | _à mesurer_ | _à mesurer_ | _à mesurer_ |
| Coût moyen par question (calculé sur mesures) | _à calculer_ | _à calculer_ | _à calculer_ | _à calculer_ |
| Latence p50 / TTFT (mesurée) | _à mesurer_ | _à mesurer_ | _à mesurer_ | _à mesurer_ |
| Qualité moyenne /5 (15 questions) | _à mesurer_ | _à mesurer_ | _à mesurer_ | _à mesurer_ |
| Support Spring AI | Starter OpenAI (déjà en place) | Starter OpenAI (déjà en place) | Starter Anthropic (nouvelle dépendance) | Starter OpenAI (en place) |

Prix relevés le 05/10/2026 sur les pages officielles des providers
(developers.openai.com/api/docs/pricing et /models, page pricing Anthropic).

Formule utilisée :

```text
coût/question = tokens_in × prix_in / 1 000 000 + tokens_out × prix_out / 1 000 000
```

(¹) Sur l'API Chat Completions, GPT-6 Luna ne supporte le function calling qu'avec
`reasoning_effort = none` ; l'API Responses le supporte à tous les niveaux. À vérifier avec
l'API utilisée par Spring AI avant NEXUS-19.
(²) Claude Sonnet 5.5 refuse le tool calling forcé (`tool_choice` `any`/`tool`) : il faut
passer par `auto` + `strict: true`, ou par le structured output.
(³) Sonnet 5.5 a le raisonnement (thinking) activé par défaut : les tokens de réflexion sont
facturés comme de l'output, le coût réel peut donc être nettement supérieur à l'estimation.
Même remarque pour les modèles OpenAI à raisonnement. C'est précisément ce que le benchmark doit mesurer.

## Décision

Nous utilisons **GPT-6.1 Sol** (`gpt-6.1-sol`) pour NEXUS v0.1 à v0.3, avec une temperature
de 0.2 si le modèle l'accepte (sinon, effort de raisonnement minimal pour limiter variance
et coût). **GPT-6 Luna** est retenu comme candidat pour le routing des tâches simples (NEXUS-27)
et **Claude Sonnet 5.5** comme modèle de secours chez un second provider (NEXUS-26).

## Justification

À prix identique, GPT-6.1 Sol et Claude Sonnet 5.5 sont équivalents sur le papier ; Sol
l'emporte parce qu'il s'intègre sans changement de dépendance (le projet est déjà sur le
starter OpenAI de Spring AI), ce qui garde NEXUS-01 → 03 focalisés sur le produit plutôt que
sur la plomberie. GPT-6 Luna est 20 fois moins cher, mais pour un projet dont la valeur repose
sur la précision des réponses techniques (incidents, architecture), la qualité prime sur le
coût tant que la facture reste de l'ordre de ~20 $ pour 1 000 questions. gpt-4o-mini est
écarté : ancienne génération, sortie limitée à 16 k et context window de 128 k qui deviendra
juste en v0.4+. La context window ne départage pas les candidats : ~7,5 k tokens en v0.3, soit
moins de 1 % de la fenêtre disponible.

## Conséquences

- **Positives**
  - Aucune migration de code : seule la propriété `spring.ai.openai.chat.model` change.
  - Modèle récent, structured output et tool calling supportés nativement : NEXUS-03 et NEXUS-19 ne sont pas bloqués.
  - Marge de context window énorme pour le RAG et l'historique de conversation.
- **Négatives / risques**
  - Coût ~14 fois supérieur à la baseline gpt-4o-mini : le coût grimpera avec le RAG (×4–5 tokens en entrée entre v0.2 et v0.3) et avec les tours d'agent en v0.7.
  - Dépendance à un seul provider (OpenAI) tant que NEXUS-26 n'est pas livré : une panne ou un changement de prix impacte directement NEXUS.
  - Le coût réel dépend des tokens de raisonnement, invisibles dans l'estimation : il peut s'écarter fortement des chiffres ci-dessus.
  - Les modèles et prix évoluent tous les quelques mois : cette comparaison sera obsolète rapidement.
  - La temperature n'est pas forcément acceptée par les modèles à raisonnement : le déterminisme se pilote alors via le prompt et le structured output.
- **Mesures**
  - Coût (tokens in/out, $) et latence loggués à chaque appel (NEXUS-03).
  - Le modèle est une propriété de configuration, jamais une constante dans le code.

## Révision prévue

- NEXUS-07 : le harness d'évaluation remplace la notation manuelle /5 par une mesure reproductible.
- NEXUS-26 : abstraction du provider (`AiModelPort`) et fallback vers Claude Sonnet 5.5.
- NEXUS-27 : routing vers GPT-6 Luna pour les tâches simples, prompt caching sur le system prompt.

Cette décision sera revue si le coût moyen par question dépasse **0,05 €**, si la latence p50
dépasse **5 s**, ou si la qualité mesurée par le harness (NEXUS-07) devient insuffisante.

---

## Annexe A — Questions de benchmark

Les mêmes questions sont envoyées à chaque modèle via `POST /ask`.

1. Explique la différence entre un HTTP 502, 503 et 504 et la cause probable de chacun derrière un load balancer.
2. `payment-service` retourne 12 % de 503 depuis 09:42, juste après un déploiement. Quelles vérifications fais-tu, dans quel ordre ?
3. Comment implémenter un retry idempotent sur un appel de paiement ?
4. Quand préférer un circuit breaker à un simple retry avec backoff ?
5. Ce code Java lève une `ConcurrentModificationException` : `for (var x : list) if (x.isExpired()) list.remove(x);` Pourquoi, et comment corriger ?
6. Explique le pattern Outbox et le problème qu'il résout.
7. Une requête PostgreSQL passe de 20 ms à 4 s après un import massif. Hypothèses ?
8. Comment structurer un runbook pour un incident de saturation de pool de connexions ?
9. Différence entre authentification par session, JWT et OAuth2 client credentials pour des appels service-à-service.
10. Rédige les 5 sections clés d'un postmortem blameless.
11. Un pod Kubernetes est en `CrashLoopBackOff`. Démarche de diagnostic.
12. Comment choisir entre Kafka et RabbitMQ pour un bus d'événements de commandes ?
13. Explique les virtual threads de Java 21 et quand ils n'apportent rien.
14. Une latence p99 augmente sans que le p50 bouge. Que cela suggère-t-il ?
15. Question piège : « Quelle est la configuration exacte du rate limiter de `orders-service` ? » (attendu : le modèle reconnaît qu'il ne sait pas).

## Annexe B — Protocole de mesure

1. Pour chaque modèle, changer `spring.ai.openai.chat.model` (ou activer le starter Anthropic pour Sonnet 5.5).
2. Envoyer les 15 questions, 2 passages chacune.
3. Relever depuis la `ChatResponse` : `usage.promptTokens`, `usage.completionTokens` (tokens de raisonnement inclus), latence totale et time-to-first-token.
4. Noter chaque réponse de 1 à 5 (exactitude, précision, JSON valide, abstention sur la question 15).
5. Calculer le coût par question avec la formule ci-dessus, puis la moyenne et la latence p50.
6. Remplir le tableau, confirmer ou corriger la décision, et passer le statut à **Accepté**.
