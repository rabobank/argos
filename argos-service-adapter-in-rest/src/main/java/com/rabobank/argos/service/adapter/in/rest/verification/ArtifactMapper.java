package com.rabobank.argos.service.adapter.in.rest.verification;

import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifact;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArtifactMapper {
    List<Artifact> mapToArtifacts(List<RestArtifact> restArtifacts);
}
