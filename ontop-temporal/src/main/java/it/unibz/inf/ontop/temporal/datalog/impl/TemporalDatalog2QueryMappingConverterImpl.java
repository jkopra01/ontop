package it.unibz.inf.ontop.temporal.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.impl.Datalog2QueryTools;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalog2QueryMappingConverter;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalogProgram2QueryConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.temporal.datalog.impl.TemporalDatalogRule2QueryConverter.convertDatalogRule;


/**
 * Convert mapping assertions from Datalog to IntermediateQuery
 *
 */
@Singleton
public class TemporalDatalog2QueryMappingConverterImpl implements TemporalDatalog2QueryMappingConverter {

    private final TemporalDatalogProgram2QueryConverter converter;
    private final SpecificationFactory specificationFactory;
    private final TemporalIntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;

    @Inject
    private TemporalDatalog2QueryMappingConverterImpl(TemporalDatalogProgram2QueryConverter converter,
                                              SpecificationFactory specificationFactory,
                                                      TemporalIntermediateQueryFactory iqFactory,
                                              ProvenanceMappingFactory provMappingFactory){
        this.converter = converter;
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
    }

    @Override
    public Mapping convertMappingRules(ImmutableList<CQIE> mappingRules, DBMetadata dbMetadata,
                                       ExecutorRegistry executorRegistry, MappingMetadata mappingMetadata) {

        ImmutableMultimap<Predicate, CQIE> ruleIndex = mappingRules.stream()
                .collect(ImmutableCollectors.toMultimap(
                        r -> r.getHead().getFunctionSymbol(),
                        r -> r
                ));

        ImmutableSet<Predicate> extensionalPredicates = ruleIndex.values().stream()
                .flatMap(r -> r.getBody().stream())
                .flatMap(Datalog2QueryTools::extractPredicates)
                .filter(p -> !ruleIndex.containsKey(p))
                .collect(ImmutableCollectors.toSet());

        Stream<IntermediateQuery> mappingStream = ruleIndex.keySet().stream()
                .map(predicate -> converter.convertDatalogDefinitions(dbMetadata,
                        predicate, ruleIndex, extensionalPredicates, Optional.empty(), executorRegistry))
                .filter(Optional::isPresent)
                .map(Optional::get);

        ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap = mappingStream
                .collect(ImmutableCollectors.toMap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q));

        return specificationFactory.createMapping(mappingMetadata, mappingMap, executorRegistry);
    }

    @Override
    public MappingWithProvenance convertMappingRules(ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap,
                                                     DBMetadata dbMetadata, ExecutorRegistry executorRegistry,
                                                     MappingMetadata mappingMetadata) {
        ImmutableSet<Predicate> extensionalPredicates = datalogMap.keySet().stream()
                .flatMap(r -> r.getBody().stream())
                .flatMap(Datalog2QueryTools::extractPredicates)
                .collect(ImmutableCollectors.toSet());


        ImmutableMap<IntermediateQuery, PPMappingAssertionProvenance> iqMap = datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> convertDatalogRule(dbMetadata, e.getKey(), extensionalPredicates, Optional.empty(),
                                iqFactory, executorRegistry),
                        Map.Entry::getValue));

        return provMappingFactory.create(iqMap, mappingMetadata, executorRegistry);
    }
}
