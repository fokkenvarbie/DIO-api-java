package one.digitalinnovation.beerstock.mapper;

import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BeerMapper {

    Beer toModel(BeerDTO beerDTO);

    BeerDTO toDTO(Beer beer);
}