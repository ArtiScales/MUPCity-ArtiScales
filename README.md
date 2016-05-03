**Changements Commit du 03/05 :** *(je l'ai mis dans le champ duc ommint, mais je sais pas si c'est entièrement consultable)*

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

PS : pour le nom des nouvelles couches et les méthodes et variables qui lui sont associés, je dois avouer qu'au début ça m'a fait assez rire de coder un truc qui s'appelle evalAnal, puis je m'y suis fait.. puis je viens de me rendre compte que vis à vis d'autre, ça pourrais porter à confusion ! Suivant vos retour, si c'est marrant ou pas, je changerai les nom pour un allongé evaluationAnalyse

