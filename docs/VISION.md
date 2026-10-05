# NEXUS --- Vision Produit

## 1. Vision

**NEXUS est une plateforme d'intelligence technique pour les équipes
Engineering.**

Elle connecte :

-   la connaissance de l'entreprise ;
-   les données opérationnelles ;
-   les outils internes ;
-   les LLM.

Son objectif est d'aider les développeurs et Tech Leads à comprendre
leur système, retrouver rapidement une information technique, analyser
un incident, investiguer un problème, consulter l'état réel des
services, proposer des actions et exécuter certaines actions sous
contrôle humain.

> **Ask. Investigate. Understand. Act.**

NEXUS n'est pas un simple chatbot branché sur un LLM. C'est une
**application métier AI** destinée aux équipes Engineering.

------------------------------------------------------------------------

## 2. Problème produit

Dans une organisation Engineering, la connaissance et les informations
opérationnelles sont dispersées entre de nombreux systèmes :

-   GitHub ;
-   documentation interne ;
-   runbooks ;
-   ADR ;
-   postmortems ;
-   Jira ;
-   monitoring ;
-   logs ;
-   APIs internes ;
-   bases de connaissances.

Lorsqu'un développeur doit comprendre un système ou investiguer un
incident, il doit naviguer entre plusieurs outils, retrouver les bonnes
informations puis établir lui-même les relations entre elles.

Exemple :

``` text
🚨 ALERT
payment-service
HTTP 503 rate > 12%
```

L'investigation traditionnelle peut nécessiter :

``` text
Grafana
   ↓
GitHub
   ↓
Documentation
   ↓
Runbook
   ↓
Jira
   ↓
Logs
   ↓
Infrastructure
```

**NEXUS devient le point d'entrée unique vers la connaissance et les
capacités techniques de l'organisation.**

------------------------------------------------------------------------

## 3. Utilisateurs

### Developer

Le développeur utilise NEXUS pour :

-   comprendre une architecture ;
-   rechercher de la documentation ;
-   analyser un bug ;
-   investiguer un incident ;
-   consulter l'état d'un service ;
-   obtenir des recommandations techniques.

### Tech Lead

Le Tech Lead utilise NEXUS pour :

-   analyser des incidents complexes ;
-   comprendre les dépendances entre systèmes ;
-   consulter les décisions d'architecture ;
-   obtenir des éléments de preuve ;
-   proposer et suivre des actions.

### Administrator

L'administrateur gère notamment :

-   utilisateurs ;
-   projets ;
-   permissions ;
-   documents ;
-   intégrations ;
-   outils disponibles ;
-   providers AI.

------------------------------------------------------------------------

## 4. Expérience principale

L'utilisateur ouvre NEXUS et dispose d'un point d'entrée conversationnel
vers son environnement Engineering.

``` text
┌───────────────────────────────────────────────────────────┐
│ NEXUS                                      User ▾         │
├────────────────┬──────────────────────────────────────────┤
│ New chat       │                                          │
│                │   How can I help?                       │
│ Conversations  │                                          │
│                │   Ask about your engineering system...  │
│ Projects       │                                          │
│                │   Suggested                             │
│ Incidents      │   ◉ Investigate an incident             │
│                │   ◉ Search documentation                │
│ Knowledge      │   ◉ Check project status                │
│                │   ◉ Explain architecture                │
│ Actions        │                                          │
└────────────────┴──────────────────────────────────────────┘
```

------------------------------------------------------------------------

## 5. Cas d'usage 1 --- Ask NEXUS

Un développeur demande :

> How does authentication work in payment-service?

NEXUS ne se contente pas des connaissances générales du modèle. Il
recherche d'abord les informations pertinentes dans la connaissance
interne.

``` text
Question
   ↓
NEXUS
   ↓
Knowledge Search
   ↓
payment-auth.md
ADR-023-oauth.md
security-runbook.md
   ↓
LLM
   ↓
Answer + Sources
```

La réponse peut indiquer les sources utilisées afin que l'utilisateur
puisse vérifier l'information.

NEXUS devient ainsi une **interface conversationnelle sur la
connaissance technique de l'entreprise**.

------------------------------------------------------------------------

## 6. Cas d'usage 2 --- Incident Investigation

L'investigation d'incidents constitue l'un des principaux cas d'usage du
produit.

Exemple :

> payment-service retourne 12 % de HTTP 503 depuis 09:42.

Le développeur demande :

> Investigate payment 503.

NEXUS orchestre plusieurs sources d'information :

``` text
                     USER
                      │
                      ▼
            "Investigate payment 503"
                      │
                      ▼
                    NEXUS
                      │
             ┌────────┼─────────┐
             │        │         │
             ▼        ▼         ▼
          Incident  Runbooks   Project
            API       RAG       API
             │        │         │
             └────────┼─────────┘
                      ▼
                   Evidence
                      │
                      ▼
                  AI Analysis
```

NEXUS peut produire :

### Incident

-   Severity: HIGH
-   Service: `payment-service`
-   Started: 09:42
-   Error: HTTP 503

### Probable cause

Exemple :

> Deployment `payment-service v2.4.1` occurred seven minutes before the
> error rate increased.

### Evidence

-   deployment v2.4.1 à 09:35 ;
-   augmentation des 503 à 09:42 ;
-   runbook mentionnant un risque de saturation du pool DB ;
-   incident historique similaire.

### Recommended actions

1.  Vérifier le pool de connexions DB.
2.  Comparer la configuration avec la version précédente.
3.  Inspecter les logs du déploiement.
4.  Évaluer un éventuel rollback.

Chaque affirmation importante doit pouvoir être reliée à une preuve.

------------------------------------------------------------------------

## 7. Cas d'usage 3 --- Données temps réel via Tools

Une question comme :

> What's the current status of payment-service?

ne doit pas être résolue uniquement via RAG, car la documentation peut
être obsolète.

NEXUS appelle un outil :

``` text
User
 ↓
NEXUS
 ↓
Tool selection
 ↓
getProjectStatus("payment")
 ↓
Internal API
 ↓
Real data
 ↓
NEXUS
 ↓
Answer
```

NEXUS distingue donc trois catégories fondamentales :

``` text
Knowledge question
        ↓
       RAG

Current information
        ↓
     READ TOOL

Perform an action
        ↓
     WRITE TOOL
```

------------------------------------------------------------------------

## 8. Cas d'usage 4 --- Actions contrôlées

Après une investigation, l'utilisateur peut demander :

> Create a P1 action for the Payment team.

NEXUS prépare l'action :

``` text
Proposed Action

Title
Investigate payment-service 503 spike

Priority
P1

Team
Payment

Status
PENDING_APPROVAL
```

Une action sensible n'est pas exécutée immédiatement.

``` text
AI proposes action
        ↓
PENDING_APPROVAL
        ↓
Developer reviews
     ┌──────┴──────┐
   Reject        Approve
                   ↓
              Tool execution
                   ↓
              Action created
```

Principe :

> **AI proposes. Humans control. Systems execute.**

------------------------------------------------------------------------

## 9. Domaines fonctionnels

NEXUS est structuré autour de six grands domaines :

``` text
                        NEXUS

 ┌────────────┬─────────────┬─────────────┐
 │            │             │             │
 ▼            ▼             ▼             ▼
Assistant   Knowledge    Incidents      Actions
 │            │             │             │
 └────────────┴──────┬──────┴─────────────┘
                     │
                     ▼
                   Tools
                     │
                     ▼
                 Integrations

──────────────────────────────────────────────

          Platform / Governance

 Security │ Evaluation │ Observability │ Cost
```

------------------------------------------------------------------------

## 10. Assistant

Le module Assistant constitue l'entrée conversationnelle de NEXUS.

Il gère notamment :

-   Conversation ;
-   Message ;
-   Prompt ;
-   Memory ;
-   Streaming ;
-   Structured Output.

Exemples :

-   Explain this architecture.
-   What caused incident INC-123?
-   How do we deploy payment-service?
-   Compare these two architecture decisions.

------------------------------------------------------------------------

## 11. Knowledge

Le module Knowledge gère la connaissance documentaire.

``` text
Project
   │
   ▼
Documents
   │
   ▼
Chunks
   │
   ▼
Embeddings
   │
   ▼
Vector Index
```

Les sources peuvent progressivement inclure :

-   Markdown ;
-   PDF ;
-   repositories Git ;
-   documentation interne ;
-   runbooks ;
-   ADR ;
-   postmortems ;
-   documentation technique.

Pipeline :

``` text
Document
   ↓
Ingestion
   ↓
Chunking
   ↓
Embedding
   ↓
Indexing
   ↓
Search
   ↓
Reranking
   ↓
Evidence
```

------------------------------------------------------------------------

## 12. Incidents

L'incident devient un véritable concept métier.

``` text
Incident

id
service
severity
status
startedAt
description
```

Une investigation suit le modèle :

``` text
Incident
   ↓
Investigation
   ↓
Evidence
   ↓
Analysis
   ↓
Recommendations
```

L'investigation n'est donc pas simplement un prompt envoyé à un modèle.

------------------------------------------------------------------------

## 13. Actions

NEXUS distingue une recommandation d'une action réelle.

``` text
Recommendation
```

Une recommandation n'a aucun effet sur le système.

Une action possède un cycle de vie :

``` text
PROPOSED
    ↓
PENDING_APPROVAL
    ↓
 ┌──┴─────┐
 ▼        ▼
REJECTED APPROVED
           ↓
        EXECUTING
           ↓
      ┌────┴────┐
      ▼         ▼
  COMPLETED   FAILED
```

------------------------------------------------------------------------

## 14. Tools

NEXUS possède un catalogue de capacités.

### Read Tools

``` text
getIncident()
getProjectStatus()
searchDocumentation()
getDeployment()
getServiceHealth()
```

### Write Tools

``` text
createActionItem()
createIncident()
triggerWorkflow()
```

Chaque tool peut définir :

-   Name ;
-   Description ;
-   Input Schema ;
-   Required Permission ;
-   READ / WRITE ;
-   Timeout ;
-   Retry Policy.

Les outils d'écriture ajoutent notamment :

-   idempotence ;
-   autorisation ;
-   approbation humaine lorsque nécessaire.

------------------------------------------------------------------------

## 15. Project comme unité organisationnelle

Le concept `Project` organise la connaissance, les incidents, les outils
et les permissions.

``` text
Project: PAYMENT

Documents
├── architecture.md
├── runbook.md
└── deployment.md

Incidents
├── INC-123
└── INC-421

Tools
├── getDeployment
├── getHealth
└── createAction

Members
├── Alice
├── Bob
└── Hamza
```

Les permissions suivent l'utilisateur :

``` text
User
 ↓
Project permissions
 ↓
Authorized knowledge
 ↓
Authorized tools
```

Un utilisateur ne doit jamais pouvoir récupérer via RAG un document
auquel il n'a pas accès.

------------------------------------------------------------------------

## 16. Architecture cible

``` text
                         WEB APP
                            │
                            ▼
                     NEXUS BACKEND
                            │
                  ┌─────────┴─────────┐
                  │                   │
                  ▼                   ▼
              REST / SSE          Security
                  │
                  ▼
              USE CASES
                  │
                  ▼
            AI ORCHESTRATOR
                  │
     ┌────────────┼────────────┬─────────────┐
     │            │            │             │
     ▼            ▼            ▼             ▼
 Knowledge     AI Model      Tools       Workflows
     │            │            │             │
     ▼            ▼            ▼             ▼
 PostgreSQL    Provider    Internal APIs   HITL
 + pgvector                 MCP
```

Les préoccupations transverses entourent l'ensemble :

``` text
Evaluation
Security
Observability
Cost
Resilience
Privacy
```

------------------------------------------------------------------------

## 17. Modèle métier cible

Le modèle peut progressivement évoluer vers :

``` text
Organization
    │
    ├──── User
    │
    └──── Project
             │
             ├──── Document
             │       │
             │       └── DocumentChunk
             │
             ├──── Incident
             │       │
             │       └── Investigation
             │               │
             │               └── Evidence
             │
             └──── Action
                     │
                     └── Approval

User
 │
 └──── Conversation
          │
          └──── Message
```

Ce modèle représente la **vision cible**. Il ne doit pas être implémenté
entièrement dès le départ : les concepts émergent au fur et à mesure des
besoins du produit.

------------------------------------------------------------------------

## 18. North Star Metric

La valeur de NEXUS n'est pas le nombre de prompts ou de tokens générés.

La métrique produit principale peut être formulée comme :

> **Percentage of engineering questions and investigations resolved with
> verifiable evidence.**

Le flux de valeur recherché est :

``` text
Developer problem
       ↓
NEXUS
       ↓
Useful answer
       +
Verifiable evidence
       +
Correct action
       ↓
Problem resolved faster
```

------------------------------------------------------------------------

## 19. Principes produit

### 1. Evidence over confidence

Une réponse vérifiable vaut mieux qu'une réponse simplement
convaincante.

### 2. Retrieve before inventing

Lorsque l'organisation possède l'information, NEXUS doit la rechercher
plutôt que l'inventer.

### 3. Tools for facts

Une information dynamique doit provenir du système réel.

### 4. Humans control writes

Les actions sensibles restent sous contrôle humain.

### 5. Authorization follows the user

NEXUS ne doit jamais accéder à davantage d'informations que
l'utilisateur qui l'interroge.

### 6. Everything important is measurable

Prompts, retrieval, tool selection, qualité, coût et latence doivent
pouvoir être évalués.

### 7. AI failure is a normal backend failure mode

Les situations suivantes font partie du design normal du système :

-   timeout ;
-   hallucination ;
-   rate limit / HTTP 429 ;
-   sortie invalide ;
-   mauvais tool ;
-   provider indisponible ;
-   contexte insuffisant.

------------------------------------------------------------------------

## 20. Evolution du produit

La vision finale est ambitieuse, mais NEXUS doit rester petit au début.

### NEXUS v0.1

``` text
Developer
    ↓
POST /api/ai/ask
    ↓
AiAssistantService
    ↓
PromptService
    ↓
ChatClient
    ↓
LLM
    ↓
Answer
```

Puis le produit évolue progressivement :

``` text
v0.1
AI Assistant
   ↓
v0.2
Reliable Assistant
   ↓
v0.3
Knowledge Assistant
   ↓
v0.4
Production RAG
   ↓
v0.5
Connected Assistant
   ↓
v0.6
Agentic Assistant
   ↓
v0.7
AI Platform / MCP
   ↓
v0.8
Production AI Platform
   ↓
v1.0
NEXUS
```

Chaque évolution doit apporter :

1.  une nouvelle capacité ;
2.  un test ou une preuve ;
3.  une mesure lorsque cela est pertinent ;
4.  une décision d'architecture documentée si nécessaire.

------------------------------------------------------------------------

## 21. Vision finale

À maturité, NEXUS doit être capable de traiter un scénario comme :

> **"payment-service retourne des HTTP 503 depuis le déploiement de ce
> matin. Analyse le problème et propose les prochaines actions."**

``` text
                   USER
                    │
                    ▼
            "Payment → 503"
                    │
                    ▼
             AI ORCHESTRATOR
                    │
          ┌─────────┼─────────┐
          ▼         ▼         ▼
      Incident    RAG      Project
        Tool      │         Tool
          │       │          │
          │   Runbooks       │
          │   Architecture   │
          │       │          │
          └───────┼──────────┘
                  ▼
               Evidence
                  │
                  ▼
           IncidentAnalysis
                  │
        ┌─────────┼─────────┐
        │         │         │
     Severity   Causes    Sources
                            │
                            ▼
                    ProposedActions
                            │
                            ▼
                     HUMAN APPROVAL
                        ┌───┴───┐
                      Reject  Approve
                                │
                                ▼
                           Write Tool
                                │
                                ▼
                         Action created
```

Autour de ce workflow :

-   Security ;
-   ACL ;
-   Evaluation ;
-   Observability ;
-   Tracing ;
-   Tokens ;
-   Cost ;
-   Latency ;
-   Idempotency ;
-   Guardrails ;
-   Resilience.

------------------------------------------------------------------------

## 22. Positionnement final

NEXUS doit démontrer qu'un ingénieur ne sait pas seulement **appeler un
LLM depuis Spring Boot**, mais qu'il sait concevoir une véritable
plateforme AI intégrée à un système d'information.

Le projet doit démontrer la maîtrise de :

``` text
Java + Spring
      +
LLM Engineering
      +
RAG
      +
Tools
      +
Agentic Workflows
      +
MCP
      +
Evaluation
      +
Security
      +
Observability
      +
Production Engineering
```

**NEXUS est à la fois un vrai produit Engineering et le fil rouge
démontrant la progression vers un profil Senior Backend AI Engineer.**
