package test;

import java.util.UUID;

import models.Problem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import api.CurrentAPI;
import api.PairingAPI;
import api.ResponseAPI;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestPairingAPI {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void testPairingSoleRandomCase(){
		instantiateTestCase(1, "random");
	}

	@Test
	public void testPairingEvenRandomCase(){
		instantiateTestCase(2, "random");
		instantiateTestCase(22, "random");
	}

	@Test
	public void testPairingOddRandomCase(){
		instantiateTestCase(11, "random");
		instantiateTestCase(3, "random");
	}

	@Test
	public void testPairingSoleLengthCase(){
		instantiateTestCase(1, "length");
	}

	@Test
	public void testPairingEvenLengthCase(){
		instantiateTestCase(2, "length");
		instantiateTestCase(22, "length");
	}

	@Test
	public void testPairingOddLengthCase(){
		instantiateTestCase(11, "length");
		instantiateTestCase(3, "length");
	}

	@Test
	public void testPairingSoleInvLengthCase(){
		instantiateTestCase(1, "inv-length");
	}

	@Test
	public void testPairingEvenInvLengthCase(){
		instantiateTestCase(2, "inv-length");
		instantiateTestCase(22, "inv-length");
	}

	@Test
	public void testPairingOddInvLengthCase(){
		instantiateTestCase(11, "inv-length");
		instantiateTestCase(3, "inv-length");
	}
	
	@Test
	public void testPairingSoleEditDistCase(){
		instantiateTestCase(1, "edit-distance");
	}

	@Test
	public void testPairingEvenEditDistCase(){
		instantiateTestCase(2, "edit-distance");
		instantiateTestCase(22, "edit-distance");
	}

	@Test
	public void testPairingOddEditDistCase(){
		instantiateTestCase(11, "edit-distance");
		instantiateTestCase(3, "edit-distance");
	}
	
	@Test
	public void testPairingSoleInvEditDistCase(){
		instantiateTestCase(1, "inv-edit-distance");
	}

	@Test
	public void testPairingEvenInvEditDistCase(){
		instantiateTestCase(2, "inv-edit-distance");
		instantiateTestCase(22, "inv-edit-distance");
	}

	@Test
	public void testPairingOddInvEditDistCase(){
		instantiateTestCase(3, "inv-edit-distance");
		instantiateTestCase(11, "inv-edit-distance");
		
	}
	

	public static void instantiateTestCase(int n, String commentAlgorithm){
		String problemUuid = addCurrentProblemToDatabase(commentAlgorithm);
		createNDummyUserResponses(n);
		PairingAPI.constructPairings(problemUuid);
		assertAllUsersGetAResponseToComment(n);
		createNDummyUserComments(n);
		assertAllUsersGetCommentsBack(n);
	}

	public static void assertAllUsersGetAResponseToComment(int n){
		for(int i = 0; i < n; i++){
			String userId = "DUMMYUSER" + i;
			String comments = PairingAPI.getPairedQuestionResponse(CurrentAPI.getCurrentProblem(), userId);
			assertTrue(comments != null);
			assertTrue(comments.length() > 0);
		}
	}
	
	public static void assertAllUsersGetCommentsBack(int n){
		for(int i = 0; i < n; i++){
			String userId = "DUMMYUSER" + i;
			String comments = PairingAPI.getPairedCommentResponse(CurrentAPI.getCurrentProblem(), userId);
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
