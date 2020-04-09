package it.unibz.inf.ontop.spec.mapping.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataLookup;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryAttributeExtractor;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.assertEquals;

public class SelectQueryAttributeExtractorTest {


    @Test
    public void test_1() throws InvalidSelectQueryException {
        ImmutableMetadataLookup metadataLookup = new ImmutableMetadataLookup(ImmutableMap.of());
        QuotedIDFactory idfac = DEFAULT_DUMMY_DB_METADATA.getQuotedIDFactory();

        SelectQueryAttributeExtractor aex = new SelectQueryAttributeExtractor(metadataLookup, idfac, TERM_FACTORY);

        ImmutableList<QuotedID> res = aex.extract("SELECT ALMAES001.IDART,\n"+
//                "     ALMAES001.CODART,\n"+
//                "     ALMAES001.IDFAMI,\n"+
//                "     ALMAES001.DESCRIP,\n"+
//                "     ALMAES001.DESCRII,\n"+
//                "     ALMAES001.MEDIDA,\n"+
//                "     ALMAES001.DESCUENTO,\n"+
//                "     ALMAES001.UNIDADES,\n"+
//                "     ALMAES001.UDSENV,\n"+
//                "     ALMAES001.IDTIPIVA,\n"+
                "     ALMAES001.UPC,\n"+
                "     ALMAES001.PVP1, ALMAES001.PVP2, ALMAES001.PVP3, ALMAES001.PVP4, ALMAES001.PVP5,\n"+
                "     to_char(ALMAES001.FECALTA,'YYYY-MM-DD') AS FECALTA,\n"+
                "     to_char(ALMAES001.FECBLO,'YYYY-MM-DD') AS FECBLO,\n"+
                "     to_char((ALMAES001.FECBLO),('YYYY-MM-DD')) AS FECBLO1,\n"+
                "     ALMAES001.STCMIN,\n"+
//                "     ALMAES001.STCMAX,\n"+
//                "     ALMAES001.PUNRUP,\n"+
//                "     ALMAES001.UBICACION,\n"+
//                "     ALMAES001.IDCTACOMPRA,\n"+
//                "     ALMAES001.IDCTAVENTA,\n"+
                "     ALESFO001.PRECIOFINAL\n"+
                "\t             \n"+
                "FROM ALMAES001 "+
                "LEFT JOIN ALESFO001 ON ALMAES001.IDART = ALESFO001.IDART" +
                ""
        );
        assertEquals(ImmutableList.of(
                idfac.createAttributeID("IDART"),
//                idfac.createAttributeID("CODART"),
//                idfac.createAttributeID("IDFAMI"),
//                idfac.createAttributeID("DESCRIP"),
//                idfac.createAttributeID("DESCRII"),
//                idfac.createAttributeID("MEDIDA"),
//                idfac.createAttributeID("DESCUENTO"),
//                idfac.createAttributeID("UNIDADES"),
//                idfac.createAttributeID("UDSENV"),
//                idfac.createAttributeID("IDTIPIVA"),
                idfac.createAttributeID("UPC"),
                idfac.createAttributeID("PVP1"),
                idfac.createAttributeID("PVP2"),
                idfac.createAttributeID("PVP3"),
                idfac.createAttributeID("PVP4"),
                idfac.createAttributeID("PVP5"),
                idfac.createAttributeID("FECALTA"),
                idfac.createAttributeID("FECBLO"),
                idfac.createAttributeID("FECBLO1"),
                idfac.createAttributeID("STCMIN"),
//                idfac.createAttributeID("STCMAX"),
//                idfac.createAttributeID("PUNRUP"),
//                idfac.createAttributeID("UBICACION"),
//                idfac.createAttributeID("IDCTACOMPRA"),
//                idfac.createAttributeID("IDCTAVENTA"),
                idfac.createAttributeID("PRECIOFINAL")), res);
    }
}
