package org.batfish.question.fibs;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Lists the forwarding table (FIB) entries of the converged data plane. */
@ParametersAreNonnullByDefault
public class FibsQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_VRFS = "vrfs";
  private static final String VRFS_ALL = ".*";

  private final @Nullable String _nodes;
  private final @Nonnull String _vrfs;

  public FibsQuestion(@Nullable String nodes, @Nullable String vrfs) {
    _nodes = nodes;
    _vrfs = firstNonNull(vrfs, VRFS_ALL);
  }

  @JsonCreator
  private static FibsQuestion jsonCreator(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_VRFS) @Nullable String vrfs) {
    return new FibsQuestion(nodes, vrfs);
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "fibs";
  }

  @JsonProperty(PROP_NODES)
  private @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_VRFS)
  public @Nonnull String getVrfs() {
    return _vrfs;
  }

  @JsonIgnore
  public @Nonnull NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }
}
