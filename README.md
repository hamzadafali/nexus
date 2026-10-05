# NEXUS

> **Ask. Investigate. Understand. Act.**

NEXUS est une plateforme d'intelligence technique conçue pour les équipes Engineering. Elle rassemble la connaissance interne, les données opérationnelles, les outils de l'entreprise et les modèles de langage afin d'offrir un point d'entrée unique pour comprendre et piloter un système logiciel.

## Vision produit

Dans une organisation Engineering, l'information utile est dispersée entre les dépôts Git, la documentation, les ADR, les runbooks, les postmortems, les outils de suivi, les logs et les plateformes de monitoring. Comprendre un service ou diagnostiquer un incident impose donc de naviguer entre plusieurs outils, puis de reconstruire manuellement le contexte.

NEXUS réduit cette fragmentation. Un développeur ou un Tech Lead formule son besoin en langage naturel ; la plateforme recherche les éléments pertinents, interroge les systèmes autorisés et restitue une réponse structurée, vérifiable et exploitable.

NEXUS n'a pas vocation à être un simple chatbot généraliste. C'est une application métier AI qui doit :

- fonder ses réponses sur les données de l'entreprise ;
- relier chaque affirmation importante à une preuve ou à une source ;
- distinguer la connaissance documentaire des informations temps réel ;
- reconnaître l'incertitude plutôt que d'inventer une réponse ;
- proposer des actions sans contourner les permissions ni le contrôle humain.

La progression produit suit quatre verbes : **demander**, **investiguer**, **comprendre**, puis **agir**. Les trois premiers cas d'usage posent les fondations de cette trajectoire.

## Les 3 premiers cas d'usage

### 1. Interroger la connaissance technique — Ask NEXUS

Un développeur pose une question sur son environnement :

> Comment fonctionne l'authentification de `payment-service` ?

NEXUS recherche d'abord les documents internes pertinents — documentation du service, ADR, runbooks ou postmortems — puis génère une réponse accompagnée de ses sources.

```text
Question → recherche documentaire → contexte pertinent → réponse + sources
```

**Valeur apportée :** réduire le temps passé à chercher l'information et rendre la connaissance technique accessible depuis une interface conversationnelle unique.

**Résultat attendu :** une réponse concise, contextualisée et vérifiable, avec une abstention explicite lorsque les sources ne permettent pas de conclure.

### 2. Investiguer un incident

À partir d'une alerte ou d'une question telle que :

> `payment-service` retourne 12 % de HTTP 503 depuis 09:42. Peux-tu investiguer ?

NEXUS rapproche les données d'incident, les déploiements récents, les logs, les runbooks et les incidents historiques. Il produit une synthèse structurée comprenant la cause probable, les preuves disponibles et les prochaines vérifications recommandées.

```text
Incident + runbooks + déploiements + logs
                    ↓
       analyse + preuves + recommandations
```

**Valeur apportée :** accélérer le diagnostic sans masquer le raisonnement ni les éléments factuels qui le soutiennent.

**Résultat attendu :** une investigation traçable qui sépare clairement les faits, les hypothèses et les recommandations.

### 3. Consulter l'état réel d'un service

Une question portant sur la situation actuelle ne peut pas être résolue à partir de la seule documentation :

> Quel est l'état actuel de `payment-service` ?

NEXUS sélectionne un outil de lecture autorisé, interroge l'API interne concernée, puis transforme les données obtenues en une réponse compréhensible.

```text
Question → sélection de l'outil → API interne → données réelles → réponse
```

**Valeur apportée :** accéder aux informations opérationnelles à jour sans connaître chaque outil ni chaque API sous-jacente.

**Résultat attendu :** une réponse basée sur l'état courant du système, qui indique l'origine et la fraîcheur des données.

Ces usages établissent une séparation essentielle :

| Besoin | Capacité NEXUS |
|---|---|
| Comprendre une information interne | Recherche documentaire (RAG) |
| Connaître l'état actuel du système | Outil de lecture |
| Modifier un système | Outil d'écriture avec validation humaine |

## Utilisateurs cibles

- **Developers** : retrouver une information, comprendre une architecture, diagnostiquer un problème et consulter l'état d'un service.
- **Tech Leads** : analyser des incidents complexes, explorer les dépendances et appuyer les décisions sur des preuves.
- **Administrators** : gérer les projets, les permissions, les sources de connaissance, les intégrations et les fournisseurs AI.

## État du projet

NEXUS est en cours de construction. La version actuelle fournit un premier endpoint Spring Boot capable d'envoyer une question à un modèle et de retourner une réponse structurée. La recherche documentaire, l'investigation multi-source et les outils temps réel décrits ci-dessus constituent la cible produit et seront livrés progressivement.

La roadmap détaillée est disponible dans [`docs/backlog.md`](docs/backlog.md) et la vision étendue dans [`docs/VISION.md`](docs/VISION.md).

## Métrique NEXUS-01 — Coût estimé par question

Le modèle actuellement configuré est `gpt-4o-mini`. Son tarif standard est de **0,15 $ par million de tokens en entrée** et **0,60 $ par million de tokens en sortie**, d'après la [tarification officielle OpenAI](https://developers.openai.com/api/docs/models/gpt-4o-mini).

Estimation de référence pour une question NEXUS :

| Hypothèse | Volume | Coût |
|---|---:|---:|
| Prompt système, contexte et question | 500 tokens en entrée | 0,000075 $ |
| Réponse structurée | 300 tokens en sortie | 0,000180 $ |
| **Total par question** | **800 tokens** | **0,000255 $ ≈ 0,00023 €** |

Calcul : `(500 × 0,15 $ + 300 × 0,60 $) / 1 000 000`.

Avec le [taux de référence BCE du 2 octobre 2026](https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.fr.html), soit `1 € = 1,1225 $`, cela représente environ :

- **0,00023 € par question** ;
- **0,23 € pour 1 000 questions** ;
- **2,27 € pour 10 000 questions**.

Cette valeur est une estimation initiale, hors recherche documentaire, embeddings, appels d'outils, stockage et infrastructure. La métrique devra être remplacée par le coût moyen réellement observé sur 20 questions dès que les tokens d'entrée et de sortie seront journalisés.

## Stack actuelle

- Java 21
- Spring Boot 4
- Spring AI
- Gradle
- OpenAI comme provider configurable

## Démarrage local

Pré-requis : Java 21 et une clé API OpenAI.

```bash
export OPENAI_API_KEY="votre-cle"
./gradlew bootRun
```

Puis envoyer une première requête :

```bash
curl -X POST http://localhost:8080/ask \
  -H 'Content-Type: application/json' \
  -d '{"ask":"Explique le problème technique suivant : une API retourne HTTP 503."}'
```

Exemple de réponse actuelle :

```json
{
  "skill": "HTTP / Résilience",
  "explanation": "Le service est temporairement indisponible.",
  "level": 2
}
```

> Ne versionnez jamais votre clé API. Elle est lue depuis la variable d'environnement `OPENAI_API_KEY`.
> 
> ## Métriques

| Ticket | Métrique | Valeur | Méthode |
|---|---|---|---|
| NEXUS-01 | Coût estimé par question | ~0,007 € | Estimation : 500 tokens in / 400 out, prix au 05/10/2026 |
| NEXUS-02 | Latence p50 | … | Mesurée sur 20 appels |
| NEXUS-03 | Coût moyen par question | … | Mesuré sur 20 questions (Micrometer) |
