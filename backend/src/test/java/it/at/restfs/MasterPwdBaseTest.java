package it.at.restfs;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import it.at.restfs.auth.AuthorizationChecker.Implementation;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import okhttp3.ResponseBody;

public class MasterPwdBaseTest extends Stage {
	
	private final static JsonFactory FACTORY = new JsonFactory();

	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {			
			System.out.println("Running");
			System.out.println("class: " + description.getClassName());
			System.out.println("method: " + description.getMethodName());
			System.out.println("--------------------------------------------------");
		}
	};

	@Delegate
	private ExpectationsBuilder expects = new ExpectationsBuilder();

	@Getter
	private UUID container;

	@Getter
	private String token;

	/*
	 * di default tutti i container vengono creati auth = MASTER_PWD
	 */

	public ExecutionContext context() {
		return context(token);
	}

	@SneakyThrows
	@Before
	public void setUp() {
		container = UUID.randomUUID();
		System.out.println("container: " + container);

		final ExecutionContext currentCtx = context("123-my-strong-password"); // make it more random please !!?
		
		createContainer(currentCtx);
		
		final ResponseBody responseBody = runCommands(
			currentCtx, buildMgmtCommand(Operation.TOKEN)
		).get(0);

		token = extractToken(FACTORY.createParser(responseBody.byteStream()));
	}

	//XXX this is not good ... !!?
	private String extractToken(JsonParser parser) throws IOException {		
		while(!parser.isClosed()){
		    JsonToken jsonToken = parser.nextToken();

		    if(JsonToken.FIELD_NAME.equals(jsonToken)){
		        String fieldName = parser.getCurrentName();
		        //System.out.println(fieldName);

		        jsonToken = parser.nextToken();

		        if("token".equals(fieldName)){
		            return parser.getValueAsString();
		        } 
		    }
		}		
		
		throw new RuntimeException("cannot resolve 'token' property");
	}

	@After
	public void tearDown() {
		/*
		 * when run test calling remote service (on another machine) this step is not
		 * available, because internally use a local file system to find difference
		 * whith expected result
		 */

		showDiff(container);
	}

	private ExecutionContext context(String authHeader) {
		return ExecutionContext.builder()
			.container(container)
//          .printResponse(true)
			.stopOnError(true)
			.type(Implementation.MASTER_PWD)
			.authHeader(authHeader)
			.build();
	}

}
