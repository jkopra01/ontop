package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * Applies the transformer to the children
 */
public class ChildTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTransformer transformer;

    public ChildTransformer(IntermediateQueryFactory iqFactory, IQTreeTransformer transformer) {
        super(iqFactory);
        this.iqFactory = iqFactory;
        this.transformer = transformer;
    }

    @Override
    protected IQTree transformLeaf(LeafIQTree leaf){
        return leaf;
    }

    @Override
    protected IQTree transformUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
        return iqFactory.createUnaryIQTree(rootNode, child.acceptTransformer(this));
    }

    @Override
    protected IQTree transformNaryCommutativeNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(
                rootNode,
                children.stream()
                        .map(transformer::transform)
                        .collect(ImmutableCollectors.toList())
        );
    }

    @Override
    protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild,
                                                       IQTree rightChild) {
        return iqFactory.createBinaryNonCommutativeIQTree(
                rootNode,
                transformer.transform(leftChild),
                transformer.transform(rightChild)
        );
    }
}
