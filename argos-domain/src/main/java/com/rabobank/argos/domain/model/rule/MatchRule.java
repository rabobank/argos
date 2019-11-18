package com.rabobank.argos.domain.model.rule;


import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.PathHelper;
import com.rabobank.argos.domain.model.Step;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author borstg
 * <p>
 * " match rules must have the format:\n\t" " MATCH <pattern> [IN
 * <source-path-prefix>] WITH" " (MATERIALS|PRODUCTS) [IN
 * <destination-path-prefix>] FROM <step>.\n"
 */


public final class MatchRule extends Rule {

    public enum DestinationType {
        PRODUCTS, MATERIALS

    }

    private final String sourcePathPrefix;
    private final String destinationPathPrefix;
    private final DestinationType destinationType;
    private final Step destinationStep;

    @Builder
    public MatchRule(String pattern, String sourcePathPrefix, String destPrefix, DestinationType destinationType,
                     Step destStep) {
        super(pattern);
        this.sourcePathPrefix = this.normalizePath(sourcePathPrefix);
        this.destinationPathPrefix = this.normalizePath(destPrefix);
        this.destinationType = destinationType;
        this.destinationStep = destStep;
    }

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String result = PathHelper.normalizePath(path);
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }

    public String getSourcePathPrefix() {
        return sourcePathPrefix;
    }

    public String getDestinationPathPrefix() {
        return destinationPathPrefix;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public Step getDestinationStep() {
        return destinationStep;
    }

    /**
     * Filters artifacts from artifact queue using rule pattern and optional rule
     * source prefix and consumes them if there is a corresponding destination
     * artifact, filtered using the same rule pattern and an optional rule
     * destination prefix, and source and destination artifacts have matching
     * hashes.
     * <p>
     * NOTE: The destination artifacts are extracted from the links dictionary,
     * using destination name and destination type from the rule data. The source
     * artifacts could also be extracted from the links dictionary, but would
     * require the caller to pass source name and source type, as those are not
     * encoded in the rule. However, we choose to let the caller directly pass the
     * relevant artifacts.
     *
     * @param artifacts Not yet consumed artifacts.
     * @return Set of consumed artifacts.
     */
    @Override
    public Set<Artifact> verify(final Set<Artifact> artifacts, final Set<Artifact> destinationMaterials, final Set<Artifact> destinationProducts) {
        Set<Artifact> consumed = new HashSet<>();
        // Extract destination artifacts from destination link
        Set<Artifact> destinationArtifacts;
        if (this.getDestinationType() == DestinationType.MATERIALS) {
            if (destinationMaterials == null) {
                return consumed;
            }
            destinationArtifacts = new HashSet<>(destinationMaterials);
        } else {
            if (destinationProducts == null) {
                return consumed;
            }
            destinationArtifacts = new HashSet<>(destinationProducts);
        }

        Set<Artifact> filteredSourceArtifacts;
        // Filter part 1 - Filter artifacts using optional source prefix, and subtract
        // prefix before filtering with rule pattern (see filter part 2) to prevent
        // globbing in the prefix.
        if (this.getSourcePathPrefix() != null) {
            Set<Artifact> dePreFixedSourceArtifacts = new HashSet<>();

            for (Artifact artifact : artifacts) {
                if (artifact.getUri().startsWith(this.getSourcePathPrefix())) {
                    dePreFixedSourceArtifacts.add(new Artifact(artifact.getUri().substring(this.getSourcePathPrefix().length()), artifact.getHash()));
                }
            }

            // re-apply prefix
            filteredSourceArtifacts = super.filterArtifacts(dePreFixedSourceArtifacts);
            Set<Artifact> preFixedSourceArtifacts = new HashSet<>();
            for (Artifact artifact : filteredSourceArtifacts) {
                preFixedSourceArtifacts.add(new Artifact(this.getSourcePathPrefix() + artifact.getUri(), artifact.getHash()));
            }
            filteredSourceArtifacts = preFixedSourceArtifacts;
        } else {
            filteredSourceArtifacts = super.filterArtifacts(artifacts);
        }

        // Iterate over filtered source paths and try to match the corresponding
        // source artifact hash with the corresponding destination artifact hash
        for (Artifact artifact : filteredSourceArtifacts) {
            // If a destination prefix was specified, the destination artifact should
            // be queried with the full destination path, i.e. the prefix joined with
            // the globbed path.
            Artifact destinationArtifact = artifact;
            if (this.getDestinationPathPrefix() != null) {
                destinationArtifact = new Artifact(this.getDestinationPathPrefix() + artifact.getUri(),
                        artifact.getHash());
            }
            // Source and destination matched, consume artifact
            if (destinationArtifacts.contains(destinationArtifact)) {
                consumed.add(artifact);
            }
        }
        return consumed;
    }

    @Override
    public String toString() {
        return "MatchRule [sourcePathPrefix=" + sourcePathPrefix + ", destinationPathPrefix=" + destinationPathPrefix
                + ", destinationType=" + destinationType + ", destinationStep=" + destinationStep + "]";
    }
}
