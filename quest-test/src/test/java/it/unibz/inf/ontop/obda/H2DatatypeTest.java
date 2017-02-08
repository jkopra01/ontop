package it.unibz.inf.ontop.obda;

/*
 * #%L
 * ontop-test
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/***
 * Tests that h2 datatypes
 */
public class H2DatatypeTest {
    static final String owlFile = "src/test/resources/datatype/datatypes.owl";
	static final String obdaFile = "src/test/resources/datatype/datetime-h2.obda";

	private QuestOWL reasoner;
	private OntopOWLConnection conn;
	Connection sqlConnection;


	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection("jdbc:h2:mem:datatypes","sa", "");
		java.sql.Statement s = sqlConnection.createStatement();

		try {
			String text = new Scanner( new File("src/test/resources/datatype/h2-datatypes.sql") ).useDelimiter("\\A").next();
			s.execute(text);
			//Server.startWebServer(sqlConnection);

		} catch(SQLException sqle) {
			System.out.println("Exception in creating db from script");
		}

		s.close();

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.build();

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();

		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();



	}

	@After
	public void tearDown() throws Exception {
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void dropTables() throws Exception {

		conn.close();
		reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			java.sql.Statement s = sqlConnection.createStatement();
			try {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			} catch (SQLException sqle) {
				System.out.println("Table not found, not dropping");
			} finally {
				s.close();
				sqlConnection.close();
			}
		}
	}


	/**
	 * Test use of date
	 * @throws Exception
	 */
	@Test
	public void testDate() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasDate ?x\n" +
                "   FILTER ( ?x = \"2013-03-18\"^^xsd:date ) .\n" +
                "}";
		String val = runQueryReturnLiteral(query);
		assertEquals("\"2013-03-18\"", val);
	}


	@Test
	public void testDate2() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x\n" +
                "WHERE {\n" +
                "   ?x a :Row; :hasDate \"2013-03-18\"^^xsd:date\n" +
                "}";
        String val = runQueryReturnIndividual(query);
        assertEquals("<http://ontop.inf.unibz.it/test/datatypes#datetime-1>", val);
    }


	private String runQueryReturnIndividual(String query) throws OWLException, SQLException {
		OntopOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			assertTrue(rs.nextRow());
			OWLIndividual ind1 = rs.getOWLIndividual("x");
			retval = ind1.toString();

		} catch (Exception e) {
			throw e;
		} finally {
			conn.close();
			reasoner.dispose();
		}
		return retval;
	}

	private String runQueryReturnLiteral(String query) throws OWLException, SQLException {
		OntopOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			assertTrue(rs.nextRow());
			OWLLiteral ind1 = rs.getOWLLiteral("x");
			retval = ind1.toString();

		} catch (Exception e) {
			throw e;
		} finally {
			conn.close();
			reasoner.dispose();
		}
		return retval;
	}



}

