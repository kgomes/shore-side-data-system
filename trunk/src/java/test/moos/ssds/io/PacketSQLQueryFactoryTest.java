package test.moos.ssds.io;

import moos.ssds.io.PacketSQLQueryFactory;
import junit.framework.TestCase;

public class PacketSQLQueryFactoryTest extends TestCase {

	public PacketSQLQueryFactoryTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSelectConstruction() {
		// Create the factory class
		PacketSQLQueryFactory packetSQLQueryFactory = new PacketSQLQueryFactory(
				new Long(100));

		// Now pull the default query
		String defaultQuery = packetSQLQueryFactory.getQueryStatement();

	}

}
