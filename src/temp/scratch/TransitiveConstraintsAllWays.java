package temp.scratch;

import ark.util.Pair;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;

public class TransitiveConstraintsAllWays {
	
	private class Entity{
		String name;
		Pair<TimeMLRelType, Entity> firstChild;
		Pair<TimeMLRelType, Entity> secondChild;
		
		public Entity(String name){
			this(name, null, null);
		}
		public Entity(String name, Entity child, TimeMLRelType rel){
			this(name, child, rel, null, null);
		}

		public Entity(String name, Entity childOne, TimeMLRelType relOne, Entity childTwo, TimeMLRelType relTwo){
			this.name = name;
			this.firstChild = new Pair<TimeMLRelType, Entity>(relOne, childOne);
			this.secondChild = new Pair<TimeMLRelType, Entity>(relTwo, childTwo);
		}
		
		public int numChildren(){
			int num = 0;
			if (firstChild.getSecond() != null)
				num++;
			if (secondChild.getSecond() != null)
				num++;
			return num;
		}
		
		public boolean addChild(Entity child, TimeMLRelType rel){
			if (firstChild.getSecond() == null)
				firstChild = new Pair<TimeMLRelType, Entity>(rel, child);
			else if (secondChild.getSecond() == null)
				secondChild = new Pair<TimeMLRelType, Entity>(rel, child);
			else
				return false;
			return true;
		}
		
		public boolean removeChild(Entity toRemove){
			if (firstChild.getSecond().equals(toRemove)){
				firstChild.setFirst(null);
				firstChild.setSecond(null);
				if (secondChild.getSecond() != null){
					firstChild.setFirst(secondChild.getFirst());
					firstChild.setSecond(secondChild.getSecond());
					secondChild.setFirst(null);
					secondChild.setSecond(null);
				}
			} else if (secondChild.getSecond().equals(toRemove)){
				secondChild.setFirst(null);
				secondChild.setSecond(null);
			} else
				return false;
			return true;
			
		}
		
		public boolean equals(Object other){
			if (!(other instanceof Entity))
				return false;
			Entity o = (Entity) other;
			if (o.name.equals(this.name))
				return true;
			return false;
		}
		
	}

	public void checkIfAllTwoPairsNeedToBeChecked() {
		// plan: find all sets of three relations
		// test constraints on those three.
		// find all rearrangements of arrows
		// on each, test constraints
		// test constraints on all directions the arrows can run
		
		TimeMLRelType[][][] relationConstraints = TLink.getTimeMLRelTypeCompositionRules();
		
		for (TimeMLRelType relOne : TLink.TimeMLRelType.values()){
			for (TimeMLRelType relTwo : TLink.TimeMLRelType.values()){
				for (TimeMLRelType relThree : TLink.TimeMLRelType.values()){
					
					// if this is true, then the other ones should be true too.
					// if it's false, the others can be true or false.
					boolean original = testSetOfThree(relOne,relTwo,relThree,relationConstraints);

					if (!original)
						continue;

					boolean[] tf = {false, true};
					// loop over the arrow changes
					for (int i = 0; i < tf.length; i++){
						for (int j = 0; j < tf.length;j++){
							for (int k=0;k<tf.length;k++){
								boolean flipOne = tf[i];
								boolean flipTwo = tf[j];
								boolean flipThree = tf[k];
								Entity eC = new TransitiveConstraintsAllWays.Entity("eC");
								Entity eB = new TransitiveConstraintsAllWays.Entity("eB", eC, relTwo);
								Entity eA = new TransitiveConstraintsAllWays.Entity("eA", eB, relOne, eC, relThree);
								//System.out.println(flipOne + " " + flipTwo + " " + flipThree);
								if (flipOne){
									eA.removeChild(eB);
									eB.addChild(eA, TLink.getConverseTimeMLRelType(relOne));
								} 
								if (flipTwo){
									eB.removeChild(eC);
									eC.addChild(eB, TLink.getConverseTimeMLRelType(relTwo));
								}
								if (flipThree){
									eA.removeChild(eC);
									eC.addChild(eA, TLink.getConverseTimeMLRelType(relThree));
								}
								String newResult = testMosaic(eA,eB,eC, relationConstraints);

								if (newResult.equals("invalid")){
									//System.out.println("found a triple where transitivity doesn't apply");
									continue;
								}
								
								if (!Boolean.parseBoolean(newResult)){
									System.out.println("Found one where there may be more info from rearranging the arrows!");
									System.out.println(flipOne + " " + flipTwo + " " + flipThree);
									System.out.println(relOne + " " + relTwo + " " + relThree);
									System.out.println();
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static String testMosaic(Entity eA,Entity eB,Entity eC, TimeMLRelType[][][] relationConstraints){
		Entity[] entities = new Entity[3];
		entities[eA.numChildren()] = eA;
		entities[eB.numChildren()] = eB;
		entities[eC.numChildren()] = eC;
		for (int i = 0; i < entities.length; i++)
			if (entities[i] == null)
				return "invalid";
		TimeMLRelType relOne = entities[2].firstChild.getSecond().equals(entities[1]) ? entities[2].firstChild.getFirst() : entities[2].secondChild.getFirst();
		TimeMLRelType relTwo = entities[1].firstChild.getFirst();
		TimeMLRelType relThree = entities[2].firstChild.getSecond().equals(entities[0]) ? entities[2].firstChild.getFirst() : entities[2].secondChild.getFirst();
		return "" + testSetOfThree(relOne, relTwo, relThree, relationConstraints);
	}

	private static boolean testSetOfThree(TimeMLRelType relOne, TimeMLRelType relTwo,
			TimeMLRelType relThree, TimeMLRelType[][][] relationConstraints) {
		if (relOne == TimeMLRelType.VAGUE || relTwo == TimeMLRelType.VAGUE || relThree == TimeMLRelType.VAGUE)
			return true;

		for (TimeMLRelType[][] compositionRule : relationConstraints) {
			TimeMLRelType firstConjunctLabel = compositionRule[0][0];
			TimeMLRelType secondConjunctLabel = compositionRule[0][1];
			TimeMLRelType[] consequentLabels = compositionRule[1];
			
			if (relOne != null && !firstConjunctLabel.equals(relOne))
				continue;
			if (relTwo != null && !secondConjunctLabel.equals(relTwo))
				continue;
			if (consequentLabels[0] == null){
				//System.out.println(" VALID TRIPLE: " + relOne + " " + relTwo + " " + relThree);
				return true;
			}
			
			for (int i = 0; i < consequentLabels.length; i++){
				if (relThree.equals(consequentLabels[i])){
					//System.out.println(" VALID TRIPLE: " + relOne + " " + relTwo + " " + relThree);
					return true;

				}
			}
		}
		//System.out.println("INVALID TRIPLE: " + relOne + " " + relTwo + " " + relThree);
		return false;
		
	}
}
