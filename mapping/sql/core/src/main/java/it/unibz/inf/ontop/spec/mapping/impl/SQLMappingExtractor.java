package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCaster;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCanonicalTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingEqualityTransformer;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;



@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SQLMappingExtractor implements MappingExtractor {

    private final SQLPPMappingConverter ppMappingConverter;
    private final OntopMappingSQLSettings settings;
    private final MappingDatatypeFiller mappingDatatypeFiller;
    private final MappingCanonicalTransformer canonicalTransformer;
    private final MappingCaster mappingCaster;
    private final MappingEqualityTransformer mappingEqualityTransformer;
    private final MetaMappingExpander expander;

    private final MappingOntologyComplianceValidator ontologyComplianceValidator;
    private final SQLMappingParser mappingParser;

    /**
     * This represents user-supplied constraints, i.e. primary
     * and foreign keys not present in the database metadata
     *
     * Can be useful for eliminating self-joins
     */
    private final ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor;
    private final TypeFactory typeFactory;

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser, MappingOntologyComplianceValidator ontologyComplianceValidator,
                                SQLPPMappingConverter ppMappingConverter, MappingDatatypeFiller mappingDatatypeFiller,
                                OntopMappingSQLSettings settings,
                                MappingCanonicalTransformer canonicalTransformer, TermFactory termFactory,
                                SubstitutionFactory substitutionFactory, RDF rdfFactory,
                                MappingCaster mappingCaster, MappingEqualityTransformer mappingEqualityTransformer,
                                SQLPPSourceQueryFactory sourceQueryFactory,
                                ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor,
                                TypeFactory typeFactory) {

        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.mappingParser = mappingParser;
        this.ppMappingConverter = ppMappingConverter;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
        this.canonicalTransformer = canonicalTransformer;
        this.mappingCaster = mappingCaster;
        this.mappingEqualityTransformer = mappingEqualityTransformer;
        this.expander = new MetaMappingExpander(termFactory, substitutionFactory, rdfFactory, sourceQueryFactory);
        this.implicitDBConstraintExtractor = implicitDBConstraintExtractor;
        this.typeFactory = typeFactory;
    }

    @Override
    public MappingAndDBParameters extract(@Nonnull OBDASpecInput specInput,
                                          @Nonnull Optional<Ontology> ontology,
                                          @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, MetadataExtractionException {

        return convertPPMapping(extractPPMapping(specInput), specInput, ontology, executorRegistry);
    }

    @Override
    public MappingAndDBParameters extract(@Nonnull PreProcessedMapping ppMapping,
                                          @Nonnull OBDASpecInput specInput,
                                          @Nonnull Optional<Ontology> ontology,
                                          @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, MetadataExtractionException {

        return convertPPMapping((SQLPPMapping) ppMapping, specInput, ontology, executorRegistry);
    }


    protected SQLPPMapping extractPPMapping(OBDASpecInput specInput)
            throws MappingIOException, InvalidMappingException {

        Optional<File> optionalMappingFile = specInput.getMappingFile();
        if (optionalMappingFile.isPresent())
            return mappingParser.parse(optionalMappingFile.get());

        Optional<Reader> optionalMappingReader = specInput.getMappingReader();
        if (optionalMappingReader.isPresent())
            return mappingParser.parse(optionalMappingReader.get());

        Optional<Graph> optionalMappingGraph = specInput.getMappingGraph();
        if (optionalMappingGraph.isPresent())
            return mappingParser.parse(optionalMappingGraph.get());

        throw new IllegalArgumentException("Bad internal configuration: no mapping input provided in the OBDASpecInput!\n" +
                " Should have been detected earlier (in case of an user mistake)");
    }


    /**
     * Converts the PPMapping into a Mapping.
     * <p>
     * During the conversion, data types are inferred and mapping assertions are validated
     */
    protected MappingAndDBParameters convertPPMapping(SQLPPMapping ppMapping,
                                                      OBDASpecInput specInput,
                                                      Optional<Ontology> optionalOntology,
                                                      ExecutorRegistry executorRegistry)
            throws MetaMappingExpansionException, MetadataExtractionException, MappingOntologyMismatchException,
            InvalidMappingSourceQueriesException, UnknownDatatypeException {

        MappingAndDBParameters mm = convert(ppMapping, specInput.getConstraintFile(), executorRegistry);

        ImmutableList<MappingAssertion> eqMapping = mappingEqualityTransformer.transform(mm.getMapping());
        ImmutableList<MappingAssertion> filledProvMapping = mappingDatatypeFiller.transform(eqMapping);
        ImmutableList<MappingAssertion> castMapping = mappingCaster.transform(filledProvMapping);
        ImmutableList<MappingAssertion> canonizedMapping = canonicalTransformer.transform(castMapping);

        // Validation: Mismatch between the ontology and the mapping
        if (optionalOntology.isPresent()) {
            ontologyComplianceValidator.validate(canonizedMapping, optionalOntology.get());
        }

        return new MappingAndDBParametersImpl(canonizedMapping, mm.getDBParameters());
    }


    private MappingAndDBParameters convert(SQLPPMapping ppMapping,
                                           Optional<File> constraintFile,
                                           ExecutorRegistry executorRegistry) throws MetadataExtractionException, InvalidMappingSourceQueriesException, MetaMappingExpansionException {

        try (Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {

            RDBMetadataProvider metadataLoader = RDBMetadataExtractionTools.getMetadataProvider(connection, typeFactory.getDBTypeFactory());
            DBParameters dbParameters = metadataLoader.getDBParameters();
            MetadataProvider implicitConstraints = implicitDBConstraintExtractor.extract(
                    constraintFile, dbParameters.getQuotedIDFactory());

            DynamicMetadataLookup metadataLookup = new DynamicMetadataLookup(metadataLoader);

            SQLPPMapping expandedPPMapping = expander.getExpandedMappings(ppMapping, connection, metadataLookup, metadataLoader.getDBParameters().getQuotedIDFactory());
            ImmutableList<MappingAssertion> provMapping = ppMappingConverter.convert(expandedPPMapping, metadataLookup, metadataLoader.getDBParameters().getQuotedIDFactory(), executorRegistry);

            // TODO: freeze before these two - make sure new relations are not inserted
            for (RelationDefinition relation : metadataLookup.getAllRelations())
                metadataLoader.insertIntegrityConstraints(relation, metadataLookup);
            implicitConstraints.insertIntegrityConstraints(metadataLookup);

            return new MappingAndDBParametersImpl(provMapping, dbParameters);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e.getMessage());
        }
    }



    private static class MappingAndDBParametersImpl implements MappingAndDBParameters {
        private final ImmutableList<MappingAssertion> mapping;
        private final DBParameters dbParameters;

        public MappingAndDBParametersImpl(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters) {
            this.mapping = mapping;
            this.dbParameters = dbParameters;
        }

        @Override
        public ImmutableList<MappingAssertion> getMapping() {
            return mapping;
        }

        @Override
        public DBParameters getDBParameters() {
            return dbParameters;
        }
    }

}
