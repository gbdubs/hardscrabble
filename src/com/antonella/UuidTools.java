package com.antonella;

public class UuidTools {
	
	public static String getUuidFromUrl(String url){	
		String[] potentials = url.split("-|/");

		int[] expected = {8, 4, 4, 4, 12};
		int[] actual = new int[potentials.length];
		for (int i = 0; i < potentials.length; i++){ actual[i] = potentials[i].length(); }
		
		int endLocation = -1;
		for(int i = 0; i < actual.length; i++){
			int e = 0;
			int a = i;
			while (expected[e] == actual[a]){
				e++; a++;
				if (e == expected.length){
					endLocation = a;
					break;
				}
			}
		}
		if (endLocation == -1)
			return null;
		
		int stringEndLocation = -1;
		int i = 0;
		while (i < endLocation){
			stringEndLocation += potentials[i].length() + 1;
			i++;
		}
		
		int stringStartLocation = stringEndLocation - 36;
		
		return url.substring(stringStartLocation, stringEndLocation);
	}
}
