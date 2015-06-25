package api;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class AuthenticationAPI {

	private static UserService userService = UserServiceFactory.getUserService();
	private static List<String> acceptedAdministrators = null;
	
	public static boolean isUserAdministrator(){
		
		User user = userService.getCurrentUser();
		
		if (acceptedAdministrators == null){
			acceptedAdministrators = new ArrayList<String>();
			acceptedAdministrators.add("dilant@cs.brandeis.edu");
			acceptedAdministrators.add("dilant@brandeis.edu");
			acceptedAdministrators.add("gward@brandeis.edu");
			acceptedAdministrators.add("grady.b.ward@gmail.com");
		}
		
		if (user == null){
			return false;
		}
		
		return acceptedAdministrators.contains(user.getEmail());
	}
}
