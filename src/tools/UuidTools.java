package tools;

import javax.servlet.http.HttpServletRequest;

public class UuidTools {
	
	/**
	 * Extracts the first UUID from any string.  Useful for parsing information when we 
	 * don't know its exact structure/composition (or don't care)
	 * 
	 * @param parseString The string from which we are going to parse our UUID
	 * @return A String representing the UUID in the parse string, or null if none were found.
	 */
	public static String parseUuidFromString(String parseString){	
		try{
			String[] potentials = parseString.split("-|/");
	
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
			
			return parseString.substring(stringStartLocation, stringEndLocation);
		} catch (Exception e){
			// Though this is a broad catch statement, it is essential to this method, which 
			// might throw any number of exceptions from out of bounds and other sources.
			
			// We can imagine this method is much more of a request, which will either succeed
			// or fail. In the case of failure, we want to inform the user of it's failure,
			// devoid of any reason as to WHY it failed.  For this reason, we cannot allow
			// exceptions to propogate up from this method.
			return null;
		}
	}
	
	/**
	 * A Utility method that invokes parseUuidFromUrl on a Requested URI
	 * 
	 * @param req The request whose URI we are examining.
	 * @return A String UUID if it was found, null otherwise
	 */
	public static String parseUuidFromRequestUrl(HttpServletRequest req){
		return parseUuidFromString(req.getRequestURI());
	}
}
