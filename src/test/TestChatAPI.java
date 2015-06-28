package test;

import static org.junit.Assert.*;
import java.util.List;
import java.util.UUID;

import models.Problem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import api.ChatAPI;
import api.CurrentAPI;
import api.PairingAPI;
import api.ResponseAPI;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestChatAPI {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
		ChatAPI.runInTestMode();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void testPairingSoleRandomCase(){
		instantiateTestCase(1, "random", 4, 1);
	}

	@Test
	public void testPairingEvenRandomCase(){
		instantiateTestCase(2, "random", 8, 2);
		instantiateTestCase(22, "random", 7, 1);
	}

	@Test
	public void testPairingOddRandomCase(){
		instantiateTestCase(11, "random", 3, 1);
		instantiateTestCase(3, "random", 10, 5);
	}

	@Test
	public void testPairingSoleLengthCase(){
		instantiateTestCase(1, "length", 7, 1);
	}

	@Test
	public void testPairingEvenLengthCase(){
		instantiateTestCase(2, "length", 8, 8);
		instantiateTestCase(22, "length", 2, 3);
	}

	@Test
	public void testPairingOddLengthCase1(){
		instantiateTestCase(11, "length", 9, 4);
	}
	
	@Test
	public void testPairingOddLengthCase2(){
		instantiateTestCase(3, "length", 5, 3);
	}

	@Test
	public void testPairingSoleInvLengthCase(){
		instantiateTestCase(1, "inv-length", 4, 2);
	}

	@Test
	public void testPairingEvenInvLengthCase(){
		instantiateTestCase(2, "inv-length", 2, 0);
		instantiateTestCase(22, "inv-length", 9, 3);
	}

	@Test
	public void testPairingOddInvLengthCase(){
		instantiateTestCase(3, "inv-length", 10, 3);
		instantiateTestCase(11, "inv-length", 2, 1);
	}
	
	@Test
	public void testPairingSoleEditDistCase(){
		instantiateTestCase(1, "edit-distance", 8, 2);
	}

	@Test
	public void testPairingEvenEditDistCase(){
		instantiateTestCase(2, "edit-distance", 9, 4);
		instantiateTestCase(22, "edit-distance", 10, 3);
	}

	@Test
	public void testPairingOddEditDistCase(){
		instantiateTestCase(3, "edit-distance", 10, 1);
		instantiateTestCase(9, "edit-distance", 11, 2);
		instantiateTestCase(11, "edit-distance", 12, 1);
	}
	
	@Test
	public void testPairingSoleInvEditDistCase(){
		instantiateTestCase(1, "inv-edit-distance", 11, 0);
	}

	@Test
	public void testPairingEvenInvEditDistCase(){
		instantiateTestCase(2, "inv-edit-distance", 12, 1);
		instantiateTestCase(22, "inv-edit-distance", 1, 1);
	}

	@Test
	public void testPairingOddInvEditDistCase(){
		instantiateTestCase(3, "inv-edit-distance", 0, 0);
		instantiateTestCase(11, "inv-edit-distance", 12, 3);
	}
	

	public static void instantiateTestCase(int n, String commentAlgorithm, int nMessagesP1, int nMessagesP2){
		addCurrentProblemToDatabase(commentAlgorithm);
		createNDummyUserResponses(n);
		PairingAPI.constructPairings();
		assertAllUsersGetAResponseToComment(n);
		createNDummyUserComments(n);
		assertAllUsersGetCommentsBack(n);
		ChatAPI.initializeChatPhase();
		sendAndRecieveChatMessages(n, nMessagesP1, nMessagesP2);	
	}

	private static void sendAndRecieveChatMessages(int nUsers, int nMessages, int alreadyRead) {	
		for(int i = 0; i < nUsers; i++){
			String userId = "DUMMYUSER" + i;
			String chatRoomUuid = ChatAPI.getMyChatRoom(userId);
			List<String> messages = ChatAPI.getMessagesForUser(chatRoomUuid, userId, 0);
			assertEquals(1, messages.size());
		}
		
		for(int i = 0; i < nUsers; i++){
			String userId = "DUMMYUSER" + i;
			String chatRoomUuid = ChatAPI.getMyChatRoom(userId);
			for (int j = 0; j < nMessages; j++){
				ChatAPI.sendMessageToChatRoom(chatRoomUuid, userId, UUID.randomUUID().toString());
			}
		}
		
		int expectedThreeCase = nMessages * 2 + 1 - alreadyRead;
		int expectedDupleCase = nMessages * 1 + 1 - alreadyRead;
		int threeCaseFound = 0;
		
		for (int i = 0; i < nUsers; i++){
			String userId = "DUMMYUSER" + i;
			String chatRoomUuid = ChatAPI.getMyChatRoom(userId);
			List<String> messagesForMe = ChatAPI.getMessagesForUser(chatRoomUuid, userId, alreadyRead);
			if (messagesForMe.size() == expectedThreeCase && threeCaseFound < 3){
				threeCaseFound++;
				assertEquals(expectedThreeCase, messagesForMe.size());
			} else if (nUsers == 1){
				assertEquals(Math.max(1 - alreadyRead, 0), messagesForMe.size());
			} else {
				assertEquals(expectedDupleCase, messagesForMe.size());
			}
		}
		
	}

	public static void assertAllUsersGetAResponseToComment(int n){
		for(int i = 0; i < n; i++){
			String userId = "DUMMYUSER" + i;
			String comments = PairingAPI.getPartnersQuestionResponse(CurrentAPI.getCurrentProblem(), userId);
			assertTrue(comments != null);
			assertTrue(comments.length() > 0);
		}
	}
	
	public static void assertAllUsersGetCommentsBack(int n){
		for(int i = 0; i < n; i++){
			String userId = "DUMMYUSER" + i;
			String comments = PairingAPI.getPartnersCommentResponse(CurrentAPI.getCurrentProblem(), userId);
			assertTrue(comments != null);
			assertTrue(comments.length() > 0);
		}
	}
	
	public static void createNDummyUserComments(int n){
		for(int i = 0; i < n; i++){
			String userId = "DUMMYUSER" + i;
			String response = UUID.randomUUID().toString();
			ResponseAPI.saveResponse(CurrentAPI.getCurrentProblem(), userId, "comment", response);
		}
	}
	
	public static void createNDummyUserResponses(int n){
		for(int i = 0; i < n; i++){
			String userId = "DUMMYUSER" + i;
			String response = UUID.randomUUID().toString();
			ResponseAPI.saveResponse(CurrentAPI.getCurrentProblem(), userId, "question", response);
		}
	}
	
	public static String addCurrentProblemToDatabase(String commentAlgorithm){
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Problem p = dummyQuestion(commentAlgorithm);
		p.save();
		
		Entity e = new Entity("Current", "Problem");
		e.setProperty("uuid", p.getUuid());
		ds.put(e);
		return p.getUuid();
	}

	public static Problem dummyQuestion(String commentAlgorithm){
		String uuid = "b7b069da-bf70-49c0-84e7-1e4190fcd665";
		Entity e = new Entity("Problem", uuid);
		e.setUnindexedProperty("uuid", uuid);
		e.setUnindexedProperty("title", "Dummy Question");
		e.setProperty("lastEdit", System.currentTimeMillis());
		e.setUnindexedProperty("preQuestion", new Text("Here is the pre-question"));
		e.setUnindexedProperty("question", new Text("HERE IS THE QUESTION"));
		e.setUnindexedProperty("postQuestion", new Text("WHAT DID YOU THINK?"));
		e.setUnindexedProperty("solution", new Text("I LIKE WAFFLES"));
		e.setUnindexedProperty("preTime", new Long(1500));
		e.setUnindexedProperty("postTime", new Long(1500));
		e.setUnindexedProperty("questionTime", new Long(3000));
		e.setUnindexedProperty("commentTime", new Long(3000));
		e.setUnindexedProperty("commentAlgorithm", commentAlgorithm);
		e.setUnindexedProperty("currentPhase", "question");
		e.setUnindexedProperty("phaseStartedAt", System.currentTimeMillis());
		e.setUnindexedProperty("problemRun", new Long(1));
		e.setUnindexedProperty("chatTime", new Long(3000));
		return new Problem(e);
	}

}
