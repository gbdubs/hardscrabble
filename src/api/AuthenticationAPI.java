package api;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class AuthenticationAPI {

	private static UserService userService = UserServiceFactory.getUserService();
	private static List<String> acceptedAdministrators = null;
	
	/**
	 * Uses User Authentication that is broader than Google App Engine's definition
	 * of an administrator.  Includes custom names (easily edited permissions), while
	 * maintaining the DevTools and Administrator Console in totally separate workspaces.
	 * @return True if the user is authorized as an instructor
	 */
	public static boolean isUserInstructor(){
		
		User user = userService.getCurrentUser();
		
		if (user == null){
			return false;
		}
		
		if (acceptedAdministrators == null){
			acceptedAdministrators = new ArrayList<String>();
			acceptedAdministrators.add("dilant@cs.brandeis.edu");
			acceptedAdministrators.add("dilant@brandeis.edu");
			acceptedAdministrators.add("gward@brandeis.edu");
			acceptedAdministrators.add("grady.b.ward@gmail.com");
			acceptedAdministrators.add("maltebar@brandeis.edu");
		}
		
		return acceptedAdministrators.contains(user.getEmail()) || userService.isUserAdmin();
	}
}
