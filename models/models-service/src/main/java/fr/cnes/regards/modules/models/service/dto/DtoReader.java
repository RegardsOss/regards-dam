/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.dto;

import java.util.ArrayList;
import java.util.List;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.dto.AttributeDto;
import fr.cnes.regards.modules.models.domain.dto.FragmentDto;

/**
 *
 * DTO helper class
 *
 * @author Marc Sordi
 *
 */
@Deprecated
public final class DtoReader {

    private DtoReader() {
    }

    /**
     * Extract {@link Fragment} from {@link FragmentDto}
     *
     * @param pFragmentDto
     *            {@link FragmentDto}
     * @return {@link Fragment}
     */
    public static Fragment toFragment(FragmentDto pFragmentDto) {
        return Fragment.buildFragment(pFragmentDto.getName(), pFragmentDto.getDescription());
    }

    /**
     * Extract {@link AttributeModel} from {@link FragmentDto}
     *
     * @param pFragmentDto
     *            {@link FragmentDto}
     * @return list of {@link AttributeModel}
     */
    public static Iterable<AttributeModel> toAttributeModels(FragmentDto pFragmentDto) {
        final Fragment fragment = toFragment(pFragmentDto);
        final List<AttributeDto> attributeDtos = pFragmentDto.getAttributes();

        final List<AttributeModel> attModels = new ArrayList<>();
        if ((attributeDtos != null) && !attributeDtos.isEmpty()) {
            for (AttributeDto dto : attributeDtos) {
                final AttributeModel attModel = AttributeModelBuilder.build(dto.getName(), dto.getType()).get();

                attModel.setAlterable(dto.isAlterable());
                attModel.setDescription(dto.getDescription());
                attModel.setFacetable(dto.isFacetable());
                attModel.setFragment(fragment);
                attModel.setOptional(dto.isOptional());
                attModel.setQueryable(dto.isQueryable());
                attModel.setRestriction(dto.getRestriction());
                attModels.add(attModel);
            }
        }
        return attModels;
    }
}
