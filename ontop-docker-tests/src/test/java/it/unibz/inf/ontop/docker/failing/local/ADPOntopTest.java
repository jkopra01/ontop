package it.unibz.inf.ontop.docker.failing.local;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.SQLExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.semanticweb.owlapi.model.OWLObject;

import java.io.File;
import java.io.FileReader;

/**
 *
 * @author 
 */
public class ADPOntopTest {
	
	final String owlFile = "/local/adp/npd-ql.owl";
	final String obdaFile = "/local/adp/mapping-fed.obda";
	final String queryFile = "/local/adp/01.q";
	final String propertyFile = "/local/adp/mapping-fed.properties";
	final String r2rmlfile = "/local/adp/mapping-fed.ttl";

	public void runQuery() throws Exception {

		String owlFileName =  this.getClass().getResource(owlFile).toString();
		String obdaFileName =  this.getClass().getResource(obdaFile).toString();
		String propertyFileName =  this.getClass().getResource(propertyFile).toString();
		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdaFileName)
				.ontologyFile(owlFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
        QuestOWL reasoner = factory.createReasoner(config);

		/*
		 * Prepare the data connection for querying.
		 */
		OntopOWLConnection conn = reasoner.getConnection();
		OntopOWLStatement st = conn.createStatement();

		String sparqlQuery = Joiner.on("\n").join(
				CharStreams.readLines(new FileReader(queryFile)));
		
		//System.out.println(sparqlQuery);
		
		try {
			QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
			int columnSize = rs.getColumnCount();
			while (rs.nextRow()) {
				for (int idx = 1; idx <= columnSize; idx++) {
					OWLObject binding = rs.getOWLObject(idx);
					System.out.print(binding.toString() + ", ");
				}
				System.out.print("\n");
			}
			rs.close();

			/*
			 * Print the query summary
			 */
			QuestOWLStatement qst = (QuestOWLStatement) st;
			String sqlQuery = ((SQLExecutableQuery)qst.getExecutableQuery(sparqlQuery)).getSQL();;

			System.out.println();
			System.out.println("The input SPARQL query:");
			System.out.println("=======================");
			System.out.println(sparqlQuery);
			System.out.println();
			
			System.out.println("The output SQL query:");
			System.out.println("=====================");
			System.out.println(sqlQuery);
			
		} finally {
			
			/*
			 * Close connection and resources
			 */
			if (st != null && !st.isClosed()) {
				st.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			reasoner.dispose();
		}
	}

  public static void main(String[] args) throws Exception {
	  new ADPOntopTest().runQuery();
	    
  }
}