/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;

import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.dto.AttributeDto;
import fr.cnes.regards.modules.models.domain.dto.FragmentDto;
import fr.cnes.regards.modules.models.domain.dto.ModelDto;

/**
 *
 * DTO helper class
 *
 * @author Marc Sordi
 *
 */
@Deprecated
public final class DtoBuilder {

    private DtoBuilder() {
    }

    /**
     * Build a {@link ModelDto} based on a {@link Model} and its related {@link ModelAttribute}
     *
     * @param pModel
     *            {@link Model}
     * @param pAttributes
     *            list of {@link ModelAttribute}
     * @return serializable {@link ModelDto}
     */
    public static ModelDto toModelDto(Model pModel, Iterable<ModelAttribute> pAttributes) {

        // Manage model
        final ModelDto modelDto = new ModelDto();
        modelDto.setName(pModel.getName());
        modelDto.setDescription(pModel.getDescription());
        modelDto.setType(pModel.getType());

        // Manage attributes from both default fragment (i.e. default namespace) and from particular fragment
        final Map<String, FragmentDto> fragmentDtoMap = new HashMap<>();

        if ((pAttributes != null) && !Iterables.isEmpty(pAttributes)) {
            for (ModelAttribute modelAtt : pAttributes) {
                dispatchAttribute(fragmentDtoMap, modelAtt);
            }
        }

        // Get default fragment
        final FragmentDto fragmentDto = fragmentDtoMap.remove(Fragment.getDefaultName());
        if (fragmentDto != null) {
            modelDto.setAttributes(fragmentDto.getAttributes());
        }

        // Manage attributes in particular fragments
        final List<FragmentDto> fragmentDtos = fragmentDtoMap.entrySet().stream().map(x -> x.getValue())
                .collect(Collectors.toList());
        if (!fragmentDtos.isEmpty()) {
            modelDto.setFragments(fragmentDtos);
        }
        return modelDto;
    }

    /**
     * Dispatch {@link ModelAttribute} in its related fragment navigating through {@link AttributeModel}
     *
     * @param pFragmentDtoMap
     *            {@link FragmentDto} map
     * @param pModelAtt
     *            {@link ModelAttribute} to dispatch
     */
    private static void dispatchAttribute(Map<String, FragmentDto> pFragmentDtoMap, ModelAttribute pModelAtt) {
        final AttributeModel attModel = pModelAtt.getAttribute();
        final Fragment fragment = attModel.getFragment();

        // Init or retrieve fragment DTO
        FragmentDto fragmentDto = pFragmentDtoMap.get(fragment.getName());
        if (fragmentDto == null) {
            // Init fragment
            fragmentDto = new FragmentDto();
            fragmentDto.setName(fragment.getName());
            fragmentDto.setDescription(fragment.getDescription());
            fragmentDto.setAttributes(new ArrayList<>());
            pFragmentDtoMap.put(fragmentDto.getName(), fragmentDto);
        }
        fragmentDto.getAttributes().add(toAttributeDTO(pModelAtt));
    }

    /**
     * Build a {@link FragmentDto} based on a {@link Fragment} and its related {@link AttributeModel}
     *
     * @param pFragment
     *            {@link Fragment}
     * @param pAttributes
     *            list of {@link AttributeModel}
     * @return serializable {@link FragmentDto}
     */
    public static FragmentDto toFragmentDto(Fragment pFragment, Iterable<AttributeModel> pAttributes) {

        // Manage fragment
        final FragmentDto dto = new FragmentDto();
        dto.setName(pFragment.getName());
        dto.setDescription(pFragment.getDescription());

        // Manage attributes
        final List<AttributeDto> attDtos = new ArrayList<>();
        if ((pAttributes != null) && !Iterables.isEmpty(pAttributes)) {
            for (AttributeModel att : pAttributes) {
                attDtos.add(DtoBuilder.toAttributeDTO(att));
            }
        }
        dto.setAttributes(attDtos);

        return dto;
    }

    private static AttributeDto toAttributeDTO(AttributeModel pAttributeModel) {
        final AttributeDto dto = new AttributeDto();
        dto.setName(pAttributeModel.getName());
        dto.setDescription(pAttributeModel.getDescription());
        dto.setAlterable(pAttributeModel.isAlterable());
        dto.setFacetable(pAttributeModel.isFacetable());
        dto.setOptional(pAttributeModel.isOptional());
        dto.setQueryable(pAttributeModel.isQueryable());
        dto.setRestriction(pAttributeModel.getRestriction());
        dto.setType(pAttributeModel.getType());
        return dto;
    }

    private static AttributeDto toAttributeDTO(ModelAttribute pModelAttribute) {
        final AttributeDto dto = toAttributeDTO(pModelAttribute.getAttribute());
        dto.setMode(pModelAttribute.getMode());
        return dto;
    }
}
