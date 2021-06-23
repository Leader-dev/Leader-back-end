package com.leader.api.service.org;

import com.leader.api.data.org.type.OrgTypeProject;
import com.leader.api.data.org.type.OrgTypeRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgTypeService {

    private final OrgTypeRepository typeRepository;

    @Autowired
    public OrgTypeService(OrgTypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    public Document getTypeAliasMapping() {
        // find all types that contains alias field
        List<OrgTypeProject> types = typeRepository.findAllByAliasNotNull(OrgTypeProject.class);

        // convert types from object list to key-value-pair object, with alias being the key
        Document typesMapping = new Document();
        types.forEach(t -> typesMapping.append(t.alias, t));
        return typesMapping;
    }
}
