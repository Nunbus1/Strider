# 🏃‍♂️ Strider

**Date de conception** : Janvier – Avril 2025  
**Finalisation** : 23 Avril 2025

## 👨‍💻 Équipe de développement

- **Romain D'Hem**  
- **Antoine Castel-Maréchal**  
- **Pier Chapon**

---

## 📝 Présentation

**Strider** est une application Android de course en direct. Elle permet à plusieurs joueurs de se connecter dans une **room**, puis de suivre en temps réel leur progression à l’aide du GPS. L'application est bâtie sur **Jetpack Compose**, **Kotlin** et **Firebase Firestore** pour une synchronisation fluide entre les participants.

### Fonctions principales :
- Créer/Rejoindre une **room** à l’aide d’un code.
- Lancer une course à plusieurs.
- Suivi **GPS** en temps réel.
- Visualisation des distances des autres joueurs.
- Podium final et affichage des stats
- Photo de profil

---
## 🧭 Structure de l'application Strider

L'application est organisée en plusieurs **pages** et **classes** assurant une séparation claire des responsabilités, de l’accueil jusqu’à l’affichage des résultats de la course.

## 📱 Pages principales

### Accueil
Permet à l’utilisateur de :
- Saisir un pseudo
- Prendre une photo de profil
- Créer ou rejoindre une room

### Create
Gère la création de room :
- Génération d’un code aléatoire ou saisie manuelle
- Initialisation de la room dans Firestore

### Lobby
Salle d’attente pour les joueurs :
- Affiche les joueurs connectés
- Permet à l’hôte de lancer la partie pour tous simultanément

### Game
Affiche les données de la course en temps réel :
- Distance parcourue par chaque joueur via un graphique
- Chronomètre synchronisé

### Finish
Présente les résultats finaux :
- Affichage du **classement** des joueurs à la fin de la course

## 🧩 Classes de gestion

### `Player`
Représente un joueur avec :
- Pseudo
- Image de profil
- S'il est hôte
- Localisation (coordonnées GPS)
- Distance parcourue
- Temps parcouru

### `Room`
Structure représentant une salle de jeu, avec :
- Code de la room
- ID de l’hôte
- Booléen indiquant si la partie a démarré
- ID du dernier joueur ayant rejoint

### `FirestoreClient`
Centralise toutes les requêtes vers Firestore :
- Création de rooms
- Récupération des joueurs
- Suivi de l’état du jeu

## ⚙️ Composants globaux

### `MainActivity`
- Gère les permissions (notamment la localisation)
- Lance les services Android nécessaires (ex. suivi GPS)

### `StriderScreen`
- Centralise la **navigation** entre les différentes pages via Jetpack Compose


## ⚙️ Fonctionnalités & Exemples de code
**Voici plusieurs fonctionnalité sensible de notre projet permettant de mieux comprendre son fonctionnement.**
### 🔥​ Gestion de la BDD avec Firebase

Ce morceau de code définit une classe FirestoreClient chargée de gérer les interactions entre l'application et la base de données Firestore. Elle centralise notamment les opérations liées aux rooms (salles de jeu), comme la lecture ou la mise à jour de leur état. Par exemple, la fonction getHostLaunchGame permet de suivre en temps réel l’état de lancement du jeu (hostLaunchGame) dans une room spécifique. Elle utilise un Flow pour émettre automatiquement les nouvelles valeurs dès qu’un changement est détecté dans Firestore, tout en gérant proprement la fin de l’écoute avec awaitClose. Cette approche réactive est idéale pour des applications temps réel comme Strider, où les joueurs doivent être notifiés instantanément du lancement d'une partie.

```kotlin

/**
 * Classe client pour interagir avec Firestore.
 * Gère les opérations de lecture, d'écriture et de mise à jour des rooms et des joueurs.
 */
@Suppress("UNCHECKED_CAST")
class FirestoreClient {

    // -------------------------
    // Firestore config
    // -------------------------

    private val tag = "FirestoreClient: "
    private val db = FirebaseFirestore.getInstance()
    private val collection = "rooms"

    /**
     * Récupère en temps réel l'état de lancement du jeu depuis Firestore.
     *
     * @param roomCode Le code de la room concernée.
     * @return Un Flow émettant true/false ou null si erreur.
     */
    fun getHostLaunchGame(roomCode: String): Flow<Boolean?> = callbackFlow {
        val roomRef = db.collection(collection).document(roomCode)
        val listener = roomRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(null)
                return@addSnapshotListener
            }
            val hostLaunchGame = snapshot?.getBoolean("hostLaunchGame")
            trySend(hostLaunchGame)
        }
        awaitClose { listener.remove() }
    }.distinctUntilChanged()
}

```
### 📍 Enregistrement des distances GPS
Ce code permet de suivre en continu la position GPS du joueur. Il utilise les services de localisation (FusedLocationProviderClient) pour recevoir régulièrement les coordonnées de l’utilisateur, et les transmet à l’objet currentPlayer via la méthode addLocation. Ce suivi permet d’alimenter en temps réel les données du joueur, essentielles pour le fonctionnement de l’application (par exemple, pour afficher sa progression dans une course). Le service inclut également une gestion des permissions, s’assurant que l’application a bien le droit d’accéder à la localisation avant de commencer les mises à jour. Grâce à ce service, l’application peut suivre les mouvements même lorsque l’utilisateur passe l’appli en arrière-plan.
```kotlin

/**
 * Service Android en tâche de fond chargé de suivre la position du joueur.
 * Envoie les coordonnées GPS au modèle de joueur courant à intervalle régulier,
 * et utilise une notification persistante pour rester actif même en arrière-plan.
 */
@Suppress("DEPRECATION")
class LocationService : LifecycleService() {

    // -------------------------
    // Gestion de la localisation
    // -------------------------

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            FirestoreClient()
            locationResult.lastLocation?.let { location ->
                PlayerManager.currentPlayer?.addLocation(location)

            }
        }
    }

    /**
     * Demande les mises à jour de localisation si la permission est accordée.
     */
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        }
    }
}
```
### 🪄​ Les LaunchedEffects

Les LaunchedEffect était très utilise pour déclencher des tâches en fonction des intéraction utilisateur. Par exemple,ce code gère le lancement de la course en deux temps : d’abord, un LaunchedEffect déclenche un compte à rebours lorsque l’hôte appuie sur "Start", en diminuant chaque seconde jusqu’à 0, avant d’appeler onStartClicked avec l’heure exacte. En parallèle, un autre LaunchedEffect écoute en temps réel Firestore pour savoir si l’hôte a lancé la course, et met à jour l’état local (hostLaunchGame). Cela permet de synchroniser tous les joueurs automatiquement.

```kotlin

    // -------------------------
    // Effet : compte à rebours au lancement
    // -------------------------

    LaunchedEffect(shouldStartCountdown.value) {
        if (shouldStartCountdown.value && !countdownStarted.value) {
            countdownStarted.value = true
            showCountdown.value = true

            while (countdown.intValue > 0) {
                delay(1000)
                countdown.value -= 1
            }

            showCountdown.value = false
            val currentTime = System.currentTimeMillis()
            onStartClicked(roomCode, playerId, currentTime)
        }
    }

     // -------------------------
    // Effet : détection du lancement par l'hôte
    // -------------------------

    LaunchedEffect(hostLaunchGame) {
        firestoreClient.getHostLaunchGame(roomCode).collectLatest { value ->
            hostLaunchGame = value
        }
    }

```
### 🔐 Création d’une room

Ce code définit deux boutons pour la création d’une room :

Le premier bouton permet à l’utilisateur d’entrer un code personnalisé. Il vérifie que le code n’est pas trop court et qu’il n’existe pas déjà dans Firestore. Si tout est bon, il crée la room avec le joueur en tant qu’hôte et stocke les infos nécessaires.

Le second bouton génère automatiquement un code unique en boucle jusqu’à trouver un code libre, puis crée la room de la même façon.
Dans les deux cas, un Toast informe l’utilisateur du résultat et la navigation se poursuit via onCreateClicked.

```kotlin
Button(
                onClick = {
                    val cleanedCode = roomCode.trim().replace("\\s".toRegex(), "")
                    if (cleanedCode.length < 6) {
                        Toast.makeText(context, "Code trop court 😅", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            val exists = firestoreClient.checkIfRoomExists(cleanedCode)
                            if (exists) {
                                Toast.makeText(context, "Code déjà existant ❌", Toast.LENGTH_SHORT).show()
                            } else {
                                val hostPlayer = Player(pseudo = pseudo, iconUrl = 1, isHost = true)
                                firestoreClient.insertRoomWithHost(cleanedCode, hostPlayer).collect { result ->
                                    if (result != null) {
                                        Toast.makeText(context, "Room créée avec le code : $cleanedCode", Toast.LENGTH_SHORT).show()
                                        IdManager.currentRoomId = cleanedCode
                                        PlayerManager.currentPlayer?.firestoreClient = firestoreClient
                                        onCreateClicked(cleanedCode, 0)
                                    } else {
                                        Toast.makeText(context, "Erreur lors de la création", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                },
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(23.dp)
            ) {
                Text(
                    "Créer la Room",
                    fontFamily = MartianMono,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        var newCode: String
                        do {
                            newCode = generateUniqueRoomCode()
                            val result = firestoreClient.insertRoomWithHost(
                                newCode,
                                Player(pseudo = pseudo, iconUrl = 1, isHost = true)
                            ).first()
                        } while (result == null)

                        roomCode = newCode
                        Toast.makeText(context, "Room créée avec le code : $newCode", Toast.LENGTH_SHORT).show()
                        IdManager.currentRoomId = newCode
                        PlayerManager.currentPlayer?.firestoreClient = firestoreClient
                        onCreateClicked(newCode, 0)
                    }
                },
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(23.dp)
            ) {
                Text(
                    "Générer un code",
                    fontFamily = MartianMono,
                    color = MaterialTheme.colorScheme.primary
                )
            }

```

### 📈​ Affichage du graphique des distances

Cette fonction SpeedGraph affiche un graphique représentant la distance m de plusieurs joueurs en fonction du temps écoulé en secondes. Les données brutes (distance/temps) sont interpolées sur 15 points pour chaque joueur sélectionné. Ensuite, le graphique est dessiné avec des axes, des labels, et des courbes colorées personnalisées pour chaque joueur. L’apparence s’adapte au mode clair/sombre, et l’interface utilise Jetpack Compose avec une Canvas pour le rendu.

```kotlin
@Composable
fun SpeedGraph(
    players: List<Pair<Int, Player>>,
    selectedPlayers: Set<Int>
) {
    // -------------------------
    // Couleurs et thème
    // -------------------------

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val lineColor = if (isDark) Color.White else Color.Black
    val pointColor = colorScheme.secondary
    val labelColor = colorScheme.secondary
    val backgroundColor = colorScheme.primary

    // -------------------------
    // Interpolation des données
    // -------------------------

    val timeSteps = 15

    val interpolatedData = remember(players, selectedPlayers) {
        players.associate { (id, player) ->
            if (id !in selectedPlayers) return@associate id to emptyList()

            val sorted = player.timedDistance.sortedBy { it.second }
            if (sorted.size < 2) return@associate id to emptyList<Pair<Float, Float>>()

            val startTime = sorted.first().second
            val endTime = sorted.last().second
            val totalDuration = endTime - startTime
            val stepDuration = totalDuration / (timeSteps - 1)

            val points = mutableListOf<Pair<Float, Float>>()
            var currentIndex = 0

            for (i in 0 until timeSteps) {
                val targetTime = startTime + i * stepDuration
                while (currentIndex < sorted.size - 2 && sorted[currentIndex + 1].second < targetTime) {
                    currentIndex++
                }

                val (d1, t1) = sorted[currentIndex]
                val (d2, t2) = sorted[currentIndex + 1]
                val tFraction = (targetTime - t1).toFloat() / (t2 - t1).toFloat()
                val interpolatedDistance = d1 + (d2 - d1) * tFraction
                val relativeTimeSec = (targetTime - startTime) / 1000f

                points.add(interpolatedDistance / 1000f to relativeTimeSec)
            }

            id to points
        }
    }

    // -------------------------
    // Échelle max pour les axes
    // -------------------------

    val allPoints = selectedPlayers.flatMap { interpolatedData[it] ?: emptyList() }
    val maxTime = allPoints.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    val maxDistance = allPoints.maxOfOrNull { it.first }?.coerceAtLeast(0.1f) ?: 0.1f

    val yLabels = List(5) { i ->
        val raw = i * maxTime / 4f
        ((raw + 9) / 10).toInt() * 10
    }
    val maxYLabel = yLabels.last()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(10.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        val graphWidth = size.width - 60f
        val graphHeight = size.height - 60f
        val originX = 40f
        val originY = size.height - 30f

        val stepY = graphHeight / maxYLabel

        yLabels.forEach { label ->
            val y = originY - (label / maxYLabel.toFloat()) * graphHeight
            drawLine(lineColor, Offset(originX, y), Offset(originX + graphWidth, y), 2f)
            drawContext.canvas.nativeCanvas.drawText(
                "$label", originX - 35f, y + 10f,
                android.graphics.Paint().apply {
                    textSize = 28f
                    color = labelColor.toArgb()
                }
            )
        }

        val firstPoints = interpolatedData[selectedPlayers.firstOrNull()] ?: emptyList()
        val totalPoints = firstPoints.size

        if (totalPoints >= 1) {
            val stepCount = 5
            for (i in 0..stepCount) {
                val fraction = i / stepCount.toFloat()
                val distanceKm = fraction * maxDistance
                val x = originX + fraction * graphWidth

                drawLine(lineColor, Offset(x, originY), Offset(x, originY - graphHeight), 2f)
                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f".format(distanceKm), x, originY + 25f,
                    android.graphics.Paint().apply {
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        color = labelColor.toArgb()
                    }
                )
            }
        }

        drawLine(lineColor, Offset(originX, originY), Offset(originX, originY - graphHeight), strokeWidth = 2f)
        drawLine(lineColor, Offset(originX, originY), Offset(originX + graphWidth, originY), strokeWidth = 2f)

        selectedPlayers.forEach { id ->
            val points = interpolatedData[id] ?: return@forEach
            if (points.size < 2) return@forEach

            val path = Path().apply {
                val (x0, y0) = points.first()
                moveTo(originX + (x0 / maxDistance) * graphWidth, originY - y0 * stepY)
                points.drop(1).forEach { (x, y) ->
                    val px = originX + (x / maxDistance) * graphWidth
                    val py = originY - y * stepY
                    lineTo(px, py)
                }
            }

            drawPath(path, Brush.linearGradient(listOf(labelColor, lineColor)), style = Stroke(width = 3f))

            points.forEach { (x, y) ->
                val px = originX + (x / maxDistance) * graphWidth
                val py = originY - y * stepY
                drawCircle(pointColor, radius = 4f, center = Offset(px, py))
            }
        }

        drawContext.canvas.nativeCanvas.apply {
            drawText("Distance (km)", originX + graphWidth / 2, originY + 50f,
                android.graphics.Paint().apply {
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = labelColor.toArgb()
                })
            save()
            rotate(90f, originX + graphWidth + 40f, originY - graphHeight / 2)
            drawText("Temps (s)", originX + graphWidth + 40f, originY - graphHeight / 2 + 15,
                android.graphics.Paint().apply {
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = labelColor.toArgb()
                })
            restore()
        }
    }
}


```

💬 Nous avons pris plaisir à développer **Strider**, et le projet nous a permis de consolider des compétences techniques tout en réalisant une application de course agréable et pratique.
