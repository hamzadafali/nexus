# Backlog NEXUS — un ticket par semaine

Source de vérité pour la création des issues GitHub. Le ticket NEXUS-NN correspond à la semaine SNN de la roadmap AI Engineering.
Rythme : lundi = théorie du sujet, mardi → vendredi ≈ 3 h de code sur le ticket.

## Versions

| Version | Nom | Tickets |
|---|---|---|
| v0.1 | AI Assistant | NEXUS-01 → 03 |
| v0.2 | Reliable Assistant | NEXUS-04 → 07 |
| v0.3 | Knowledge Assistant | NEXUS-08 → 13 (jalon public 1) |
| v0.4 | Production RAG | NEXUS-14 → 17 |
| v0.5 | Connected Assistant | NEXUS-18 → 21 |
| v0.6 | AI Platform / MCP | NEXUS-22 |
| v0.7 | Agentic Assistant | NEXUS-23 → 25 (jalon public 2) |
| v0.8 | Production AI Platform | NEXUS-26 → 30 |
| v1.0 | NEXUS | NEXUS-31 → 33 (jalon public 3) |

---

## Phase 1 — Assistant fiable

### NEXUS-01 — Cadrer NEXUS : vision, 3 cas d'usage, choix du modèle
- Semaine : S1
- Version : v0.1
- Statut : Fait
- Sujets techniques : LLM, tokens, context window, inference, temperature, hallucination
- Ressource : documentation conceptuelle Spring AI + docs du provider
- Critères d'acceptation :
  - README avec la vision produit et les 3 premiers cas d'usage
  - ADR-001 : choix du modèle (coût, context window, latence)
- Métrique de fin : coût estimé par question (€)

### NEXUS-02 — Ask NEXUS v0 : un développeur pose une question et obtient une réponse
- Semaine : S2
- Version : v0.1
- Statut : Fait
- Sujets techniques : ChatModel, ChatClient, Prompt, ChatResponse
- Ressource : https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Critères d'acceptation :
  - `POST /api/ai/ask` opérationnel
  - Test d'intégration avec un modèle mocké
  - Une erreur du provider renvoie une réponse 503 propre
- Métrique de fin : latence p50

### NEXUS-03 — NEXUS répond comme un assistant engineering, dans un format exploitable par le front
- Semaine : S3
- Version : v0.1
- Statut : En cours
- Sujets techniques : System/User messages, PromptTemplate, variables, instructions vs données, Structured Output (JSON Schema → record), mesure tokens/latence/coût
- Ressources : https://docs.spring.io/spring-ai/reference/api/chatclient.html · https://docs.spring.io/spring-ai/reference/api/structured-output.html · https://docs.spring.io/spring-ai/reference/observability/
- Critères d'acceptation :
  - System prompt versionné en ressource (pas en dur dans le code)
  - La question utilisateur n'est jamais concaténée aux instructions
  - Réponse = record `AskResponse(answer, confidence, followUps)`
  - Tokens in/out, latence et coût loggués par appel (Micrometer)
- Métrique de fin : tokens et coût moyens sur 20 questions

### NEXUS-04 — Le front ne reçoit jamais une réponse cassée
- Semaine : S4
- Version : v0.2
- Statut : À faire
- Sujets techniques : validation, schema validation, retry, refusal, fallback
- Ressource : https://docs.spring.io/spring-ai/reference/api/structured-output.html
- Critères d'acceptation :
  - Bean Validation sur le record de réponse
  - 1 retry avec message de correction si le JSON est invalide
  - Refus du modèle → statut `REFUSED`
  - Modèle de secours configurable
- Métrique de fin : taux de sorties valides sur 50 appels (cible ≥ 98 %)

### NEXUS-05 — L'utilisateur voit la réponse s'écrire en direct
- Semaine : S5
- Version : v0.2
- Statut : À faire
- Sujets techniques : API HTTP brute du provider, messages, roles, usage, finish reason, 429, streaming SSE
- Ressource : documentation API officielle du provider
- Critères d'acceptation :
  - Client HTTP minimal sans Spring AI (exercice de compréhension)
  - `POST /api/ai/ask/stream` en SSE
  - `finish_reason=length` géré explicitement
  - 429 → backoff exponentiel
- Métrique de fin : time-to-first-token p50

### NEXUS-06 — L'utilisateur reprend une conversation là où il l'avait laissée
- Semaine : S6
- Version : v0.2
- Statut : À faire
- Sujets techniques : chat memory, persistance, sliding window, résumé, token budget
- Ressource : Spring AI — Chat Memory + Advisors
- Critères d'acceptation :
  - Entités `Conversation` et `Message` en PostgreSQL
  - Fenêtre glissante de messages
  - Résumé automatique au-delà du budget
  - Plafond de tokens par requête appliqué
- Métrique de fin : tokens envoyés au 20ᵉ message, avec vs sans fenêtre

### NEXUS-07 — Monde simulé + harness d'évaluation : NEXUS devient mesurable
- Semaine : S7
- Version : v0.2
- Statut : À faire
- Sujets techniques : golden dataset, non-déterminisme, métriques, evaluation harness
- Ressource : Spring AI — Model Evaluation + documentation Evals du provider
- Critères d'acceptation :
  - Seed : 3 projets (PAYMENT, ORDERS, AUTH), 20 docs (runbooks, ADR, postmortems), 10 incidents, historique de déploiements
  - 30 questions golden avec réponse attendue et sources attendues
  - Harness exécuté en CI, 3 exécutions pour mesurer la variance
- Métrique de fin : score baseline sans RAG (point de départ volontairement bas)

## Phase 2 — Knowledge Assistant

### NEXUS-08 — Retrouver un document par le sens, pas par mot-clé
- Semaine : S8
- Version : v0.3
- Statut : À faire
- Sujets techniques : embeddings, cosine similarity, vector search, top-K, HNSW, pgvector
- Ressource : Spring AI — Embeddings / Vector Store + documentation pgvector
- Critères d'acceptation :
  - pgvector + index HNSW
  - `GET /api/knowledge/search` renvoie le top-K avec score
  - Test : question → document attendu
- Métrique de fin : Recall@5 initial sur le golden dataset

### NEXUS-09 — Importer la documentation de l'équipe (Markdown, PDF)
- Semaine : S9
- Version : v0.3
- Statut : À faire
- Sujets techniques : parsing de documents (PDF, tableaux, OCR), chunking structurel, ingestion asynchrone
- Ressource : Spring AI — ETL Pipeline + doc d'un parser (Tika, Docling…)
- Critères d'acceptation :
  - Upload asynchrone avec statut `PENDING / INDEXED / FAILED`
  - Chunking par titres
  - Tableaux PDF conservés
  - Métadonnées `project`, `docType`, `source`
- Métrique de fin : temps d'ingestion des 20 docs ; % de chunks exploitables

### NEXUS-10 — La connaissance reste à jour quand un document change
- Semaine : S10
- Version : v0.3
- Statut : À faire
- Sujets techniques : update/delete, embedding versioning, reindexing
- Ressource : Spring AI — ETL Pipeline + documentation pgvector
- Critères d'acceptation :
  - Modifier un doc remplace ses chunks sans doublon
  - Colonne `embedding_model_version`
  - Réindexation vers un nouveau modèle sans coupure (double index puis bascule)
- Métrique de fin : 0 chunk orphelin après 10 mises à jour

### NEXUS-11 — Ask NEXUS : réponse fondée sur la doc interne, avec sources
- Semaine : S11
- Version : v0.3
- Statut : À faire
- Sujets techniques : RAG (Retrieve → Augment → Generate), citations, provenance, documents non fiables (injection indirecte)
- Ressource : Spring AI — RAG / RetrievalAugmentationAdvisor + OWASP LLM01
- Critères d'acceptation :
  - Réponse avec sources `[n]` reliées aux chunks
  - Contenu des docs passé comme données, jamais comme instructions
  - Un document piégé du seed ne change pas le comportement de NEXUS
- Métrique de fin : % de réponses golden avec au moins une source correcte

### NEXUS-12 — Choisir le chunking sur des chiffres, pas à l'intuition
- Semaine : S12
- Version : v0.3
- Statut : À faire
- Sujets techniques : Recall@K, MRR, stratégies de chunking
- Ressource : ressources IR/RAG + golden dataset NEXUS
- Critères d'acceptation :
  - Le harness calcule Recall@1, Recall@5 et MRR
  - 3 stratégies comparées (taille fixe, par titre, par titre + overlap)
  - ADR du choix de chunking
- Métrique de fin : Recall@5 et MRR de la stratégie retenue

### NEXUS-13 — Release v0.3 publique
- Semaine : S13
- Version : v0.3
- Statut : À faire
- Sujets techniques : consolidation S1–S12 (aucune nouvelle ressource)
- Critères d'acceptation :
  - Repo public
  - README : architecture, démo, métriques
  - Article ou post LinkedIn
- Métrique de fin : tableau des métriques NEXUS-03 → NEXUS-12

## Phase 3 — Production RAG

### NEXUS-14 — Chaque développeur ne voit que les projets dont il est membre
- Semaine : S14
- Version : v0.4
- Statut : À faire
- Sujets techniques : metadata filtering, multi-tenant retrieval, document ACL
- Ressource : Spring AI — VectorStore Metadata Filters + PostgreSQL
- Critères d'acceptation :
  - Entités `User`, `Project`, `Membership`
  - Filtre projet appliqué côté serveur avant le retrieval, jamais décidé par le LLM
  - Un non-membre de PAYMENT ne récupère aucun chunk PAYMENT, même avec une question ciblée
- Métrique de fin : 0 fuite sur 20 questions adverses

### NEXUS-15 — Retrouver les identifiants exacts (INC-183, codes d'erreur, noms de services)
- Semaine : S15
- Version : v0.4
- Statut : À faire
- Sujets techniques : hybrid search, PostgreSQL Full Text Search / BM25, Reciprocal Rank Fusion
- Ressource : PostgreSQL Full Text Search + pgvector
- Critères d'acceptation :
  - Colonne `tsvector` + fusion RRF avec la recherche vectorielle
  - Questions golden contenant des identifiants
- Métrique de fin : Recall@5 avant/après, isolé sur les questions à identifiants

### NEXUS-16 — Comprendre les questions floues ou dépendantes de la conversation
- Semaine : S16
- Version : v0.4
- Statut : À faire
- Sujets techniques : query rewriting, multi-query, reranking
- Ressource : Spring AI — Modular RAG / Advisors
- Critères d'acceptation :
  - Réécriture de la question avec l'historique de conversation
  - Multi-query
  - Reranker activable par configuration
- Métrique de fin : gain de MRR face au surcoût en latence et en €

### NEXUS-17 — NEXUS dit « je ne sais pas » plutôt que d'inventer
- Semaine : S17
- Version : v0.4
- Statut : À faire
- Sujets techniques : groundedness, faithfulness, abstention, Evidence Gate, LLM-as-Judge (bases)
- Ressource : Spring AI — Evaluation
- Critères d'acceptation :
  - Sous un seuil de pertinence, abstention explicite
  - 5 questions golden sans réponse dans la doc
  - Juge LLM qui note la faithfulness
- Métrique de fin : North Star offline (% de réponses dont toutes les affirmations importantes sont sourcées) ; taux d'abstention correcte

## Phase 4 — Connected, MCP, Agentic

### NEXUS-18 — NEXUS résiste aux attaques avant de recevoir des tools
- Semaine : S18
- Version : v0.5
- Statut : À faire
- Sujets techniques : prompt injection directe et indirecte, untrusted docs/tools, excessive agency
- Ressource : OWASP Top 10 for LLM Applications
- Critères d'acceptation :
  - 15 cas d'attaque dans le harness (directe, indirecte via doc, tentative de lire un autre projet)
  - Contrôles en entrée et en sortie
- Métrique de fin : % d'attaques bloquées

### NEXUS-19 — « What's the status of payment-service ? » renvoie l'état réel
- Semaine : S19
- Version : v0.5
- Statut : À faire
- Sujets techniques : Function/Tool Calling, arguments, tool result, tool selection
- Ressource : https://docs.spring.io/spring-ai/reference/api/tools.html
- Critères d'acceptation :
  - Read tools `getProjectStatus` et `getIncident` sur l'API simulée
  - Question d'état → tool ; question de connaissance → RAG
- Métrique de fin : précision de sélection du tool sur 20 questions

### NEXUS-20 — Les tools agissent toujours au nom de l'utilisateur, jamais au-delà
- Semaine : S20
- Version : v0.5
- Statut : À faire
- Sujets techniques : tool authorization, identity propagation, idempotency
- Ressource : Spring Security + Spring AI Tool Calling
- Critères d'acceptation :
  - Identité propagée au tool, jamais fournie par le LLM
  - Permission requise par tool
  - `createActionItem` avec clé d'idempotence
- Métrique de fin : 0 doublon sur 10 rejeux ; 0 appel non autorisé

### NEXUS-21 — « Create a P1 action for the Payment team » passe par une approbation
- Semaine : S21
- Version : v0.5
- Statut : À faire
- Sujets techniques : routing RAG / read tool / write tool, human-in-the-loop, state machine
- Ressource : Spring AI Tools + patterns FSM / domain modeling
- Critères d'acceptation :
  - Entité `Action` avec la FSM PROPOSED → PENDING_APPROVAL → APPROVED/REJECTED → EXECUTING → COMPLETED/FAILED
  - Transitions invalides refusées
  - Endpoints approve / reject
  - Exécution uniquement après `APPROVED`
- Métrique de fin : 100 % des writes passent par l'approbation (test)

### NEXUS-22 — Les capacités de NEXUS utilisables depuis un IDE ou un assistant externe
- Semaine : S22
- Version : v0.6
- Statut : À faire
- Sujets techniques : MCP (client, server, tool, resource, prompt), MCP server Spring, sécurité
- Ressource : https://docs.spring.io/spring-ai/reference/guides/getting-started-mcp.html
- Critères d'acceptation :
  - MCP server exposant les read tools et les runbooks en resource
  - Authentification
  - Testé depuis un client MCP
- Métrique de fin : résultats identiques via MCP et via l'API

### NEXUS-23 — « Investigate payment 503 » produit une investigation sourcée
- Semaine : S23
- Version : v0.7
- Statut : À faire
- Sujets techniques : Advisors, advisor chain, workflow agentique PLAN → EXECUTE → OBSERVE → DECIDE, budgets, max steps
- Ressource : https://docs.spring.io/spring-ai/reference/api/advisors.html + docs provider sur agents/tool use
- Critères d'acceptation :
  - `Investigation` comme objet métier (Evidence, Analysis, Recommendations)
  - Boucle plan → execute → observe → decide, 6 steps max et budget de tokens
  - Advisors pour logs, sécurité et coût
  - Chaque affirmation reliée à une Evidence
- Métrique de fin : coût et nombre de steps moyens par investigation

### NEXUS-24 — Savoir si les investigations trouvent la bonne cause
- Semaine : S24
- Version : v0.7
- Statut : À faire
- Sujets techniques : agent evaluation, trajectory evaluation, tool-call evaluation
- Ressource : docs Evaluation + golden dataset NEXUS
- Critères d'acceptation :
  - 8 scénarios d'incident golden avec tools attendus et cause attendue
  - Score de trajectoire dans le harness
- Métrique de fin : % de causes correctes ; tool calls inutiles par investigation

### NEXUS-25 — Release v0.7 publique
- Semaine : S25
- Version : v0.7
- Statut : À faire
- Sujets techniques : consolidation S14–S24 (aucune nouvelle ressource)
- Critères d'acceptation :
  - Démo vidéo de 2 min
  - Article (sécurité des tools ou évaluation d'agents)
- Métrique de fin : tableau des métriques NEXUS-13 → NEXUS-24

## Phase 5 — Production AI Platform

### NEXUS-26 — NEXUS continue de répondre quand le provider tombe
- Semaine : S26
- Version : v0.8
- Statut : À faire
- Sujets techniques : ports/adapters AI, provider abstraction, timeout/retry/fallback, arbitrage fine-tuning vs RAG vs prompting
- Ressource : Spring AI Model API + Resilience4j
- Critères d'acceptation :
  - Port `AiModelPort` et deux adapters
  - Resilience4j (timeout, circuit breaker)
  - Bascule testée en coupant le provider principal
  - ADR fine-tuning vs RAG vs prompting
- Métrique de fin : % de requêtes servies, provider principal coupé

### NEXUS-27 — Diviser le coût par requête sans perdre en qualité
- Semaine : S27
- Version : v0.8
- Statut : À faire
- Sujets techniques : token economics, prompt caching, semantic cache, model routing
- Ressource : documentation provider (pricing, caching, models) + Spring AI
- Critères d'acceptation :
  - Prompt caching sur le system prompt
  - Cache sémantique cloisonné par projet et ACL
  - Petit modèle pour le routing
- Métrique de fin : coût par requête avant/après, score harness inchangé

### NEXUS-28 — Une régression de qualité bloque le déploiement
- Semaine : S28
- Version : v0.8
- Statut : À faire
- Sujets techniques : LLM-as-Judge, biais, variance, quality gates
- Ressource : Spring AI Evaluation + docs Evals du provider
- Critères d'acceptation :
  - Juge calibré contre 20 notes humaines
  - Variance mesurée sur 3 exécutions
  - La CI échoue si la faithfulness baisse au-delà d'un seuil
- Métrique de fin : taux d'accord juge / humain

### NEXUS-29 — Tout voir sans exposer de données personnelles
- Semaine : S29
- Version : v0.8
- Statut : À faire
- Sujets techniques : observabilité (traces, p95, coût, observations tool/RAG), PII, rétention, suppression, RGPD
- Ressource : https://docs.spring.io/spring-ai/reference/observability/ + Micrometer/OpenTelemetry + CNIL
- Critères d'acceptation :
  - Trace OpenTelemetry par requête (retrieval, tool, LLM)
  - Dashboard coût et p95
  - Masquage des PII avant l'envoi au provider
  - Rétention des conversations et suppression à la demande
- Métrique de fin : p95 par cas d'usage ; 0 PII dans les traces (test)

### NEXUS-30 — NEXUS tient 100 développeurs en même temps
- Semaine : S30
- Version : v0.8
- Statut : À faire
- Sujets techniques : concurrence, streaming sous charge, rate limiting, 429, Virtual Threads
- Ressource : Java Virtual Threads + Gatling + rate limits du provider
- Critères d'acceptation :
  - Virtual Threads activés
  - Rate limit par utilisateur
  - Scénario Gatling de 100 utilisateurs en streaming
- Métrique de fin : p95, taux d'erreur, débit

## Phase 6 — NEXUS v1.0 et entretiens

### NEXUS-31 — Un dossier d'architecture qu'un CTO pourrait relire
- Semaine : S31
- Version : v1.0
- Statut : À faire
- Sujets techniques : architecture AI complète, C4, ADR, system design (100k docs, multi-tenant, long-context vs RAG, scaling)
- Ressource : C4 Model + ADR + architecture NEXUS
- Critères d'acceptation :
  - Diagrammes C4 niveaux 1 à 3
  - ADRs consolidés
  - Note de dimensionnement pour 100k documents
- Métrique de fin : nombre d'ADRs (cible ≥ 10)

### NEXUS-32 — NEXUS v1.0 lançable par n'importe qui
- Semaine : S32
- Version : v1.0
- Statut : À faire
- Sujets techniques : intégration RAG + Tools + Agent + HITL + Evaluation, README, CI, Docker
- Ressource : repo NEXUS uniquement
- Critères d'acceptation :
  - `docker compose up` lance tout avec le seed
  - CI complète (tests + harness + quality gate)
  - README avec le tableau final des métriques
- Métrique de fin : temps de lancement from scratch (cible < 5 min)

### NEXUS-33 — Défendre NEXUS en entretien AI System Design
- Semaine : S33
- Version : v1.0
- Statut : À faire
- Sujets techniques : révision complète + entretiens blancs
- Ressource : notes + ADR + golden dataset + architecture NEXUS
- Critères d'acceptation :
  - 3 entretiens blancs chronométrés
  - Pitch NEXUS de 2 min et de 10 min
- Métrique de fin : grille d'auto-évaluation par entretien
