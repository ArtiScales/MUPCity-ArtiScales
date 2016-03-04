###MODIFICATIONS

- Création de la classe ExplorationTest faisant des boucles pour simuler une réplication de trois scénarios par paramètres définies
- Ajout/modif de méthodes dans AHP : 
		setCoeff pour entrer directement les eigenvector
		modif dans getCoeffs pour donner directement les vecteurs propres si ils sont définis
		ajout de variables d'état
- Ajout d'une methode ScenarioAuto.save(Project, File)
- Erreur sur les layers exporté, la couche de bus était confondue avec la couche de train. Inversion dans l'enumeration de Project


###TODO
 - le test ne peut pas aller jusqu'au bout car la mémoire sature (heap space, après env 120 scénarios simulés) . Trouver ce qui reste coincé dans la mémoire vive.
 - les raster d'évaluation sont vide
 - Virer les messages du moniteurs lors de chargement volumineux de couches
 - Il semble compliqué (et surtout très long) de réaliser les calculs avec des aménités de niveau 3. Possibilité d'optimisation de cette classe?
 
 ###END
