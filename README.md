**Changements Commit du 03/05 :** *(je l'ai mis dans le champ du commit, mais je sais pas si c'est consultable)*

création des méthodes pour analyser les données
	ScenarioAuto.extractEvalAnal
	variables dans project et autre méthodes associés
création de la classe traitement_stats
pontage de tous les monitors restants(a finir)

Suite aux récents changements d'orientations pour effectuer le traitement des données, j'avais surchargé ou ajouté quelques variables dans la classe projet, qui ne sont plus utile dorénavant.
Ajout de la couche typo et du layer associé pour pouvoir discrétiser les résultats en fonction du tissus urbain 
overload Project.load pour pouvoir charger un projet a partir de données qui sont toutes dans le même dossier, ce qui ne sert plus maintenant.. 
ajout de la variable project.expoTest pour des raisons similaires lors de la création d'un projet. 
Dois je supprimer ces changements ou peut on dire que ça pourrais être utilisé dans une hypothétique version en ligne de commande ? 

**PTRule** 
J'ai changé la valeur minimale de la règle d'acces au transports en communs de rule.PTRule pour ne pas (trop) défavoriser les évaluations avec l'opérateur de Yager

**explorationTest** 
j'ai fixé les seeds (a une valeur de 1,2et3..) pour qu'on puisse comparer entre eux les scénarios sans aléatoire. Ça peut être intéressant de voir avec des seeds aléatoire, ça sera simple à refaire. 
Je pense avoir fixé le heapspace, venant des layers qui s'accumulaient (je vais lancer le calcul ce soir, réponse demain!) 
j'avais aussi ajouté des petites variables qui simplifiait les tests pour faire des tests

Dans le **package analyse**, des miettes de msca dans Main_analyse et la classe qui va vraiment faire les analyses, mais ou il n'y a pour l'instant rien dedans.

=======
###MODIFICATIONS

- Création de la classe ExplorationTest faisant des boucles pour simuler une réplication de trois scénarios par paramètres définies
- Ajout/modif de méthodes dans AHP : 
		setCoeff pour entrer directement les eigenvector
		modif dans getCoeffs pour donner directement les vecteurs propres si ils sont définis
		ajout de variables d'état
- Ajout d'une methode ScenarioAuto.save(Project, File)
- Erreur sur les layers exporté, la couche de bus était confondue avec la couche de train. Inversion dans l'enumeration de Project


###TODO
 - Virer les messages du moniteurs lors de chargement volumineux de couches
 - Il semble compliqué (et surtout très long) de réaliser les calculs avec des aménités de niveau 3. Possibilité d'optimisation de cette classe?


