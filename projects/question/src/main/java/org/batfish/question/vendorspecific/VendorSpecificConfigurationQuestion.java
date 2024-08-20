package org.batfish.question.vendorspecific;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.MoreObjects.firstNonNull;

public final class VendorSpecificConfigurationQuestion extends Question {
    private static final String PROP_NAMES = "names";
    private static final String PROP_NODES = "nodes";

    private final @Nonnull String _names;
    private final @Nonnull NodesSpecifier _nodes;

    @JsonCreator
    private static VendorSpecificConfigurationQuestion jsonCreator(
            @JsonProperty(PROP_NAMES) @Nullable String names,
            @JsonProperty(PROP_NODES) @Nullable NodesSpecifier nodes
    ) {
        String actualNames = Strings.isNullOrEmpty(names) ? ".*" : names;
        NodesSpecifier actualNodes = firstNonNull(nodes, NodesSpecifier.ALL);
        return new VendorSpecificConfigurationQuestion(
                actualNames,
                actualNodes
        );
    }

    public VendorSpecificConfigurationQuestion(
            @Nonnull String names,
            @Nonnull NodesSpecifier nodes
    ) {
        _names = names;
        _nodes = nodes;
    }

    @Override
    public boolean getDataPlane() {
        return false;
    }

    @Override
    public String getName() {
        return "vendorSpecificConfiguration";
    }

    @JsonProperty(PROP_NAMES)
    public @Nonnull String getNames() {
        return _names;
    }

    @JsonProperty(PROP_NODES)
    public @Nonnull NodesSpecifier getNodes() {
        return _nodes;
    }
}